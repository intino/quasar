package io.quassar.editor.box.languages;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.AlexandriaWebSocket;
import io.intino.ls.IntinoLanguageServer;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class LanguageServerWebSocketHandler implements AlexandriaWebSocket {
	public static final int MessageSize = 1_000_000;
	private final Function<Session, LanguageServer> provider;
	private final Map<Session, ExecutorService> executorServiceMap = new HashMap<>();
	private final Map<Session, PipedOutputStream> clientOutputMap = new HashMap<>();
	private final Map<Session, PipedInputStream> serverInputMap = new HashMap<>();
	private final Object monitor = new Object();

	public LanguageServerWebSocketHandler(Function<Session, LanguageServer> provider) {
		this.provider = provider;
	}

	@Override
	public void onWebSocketConnect(Session session) {
		try {
			synchronized (monitor) {
				session.setIdleTimeout(Duration.ofDays(1));
				session.setMaxTextMessageSize(MessageSize);
				PipedInputStream clientInput = new PipedInputStream(MessageSize);
				clientOutputMap.put(session, new PipedOutputStream(clientInput));
				serverInputMap.put(session, new PipedInputStream(MessageSize));
				PipedOutputStream out = new PipedOutputStream(serverInputMap.get(session));
				executorServiceMap.put(session, Executors.newSingleThreadExecutor());
				executorServiceMap.get(session).submit(() -> notificationThread(session));
				IntinoLanguageServer server = (IntinoLanguageServer) provider.apply(session);
				Launcher<LanguageClient> serverLauncher = LSPLauncher.createServerLauncher(server, clientInput, out);
				server.connect(serverLauncher.getRemoteProxy());
				serverLauncher.startListening();
			}
		} catch (IOException ignored) {
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	@Override
	public void onWebSocketText(Session session, String message) {
		try {
			var content = "Content-Length: " + message.getBytes().length + "\n\n" + message;
			clientOutputMap.get(session).write(content.getBytes());
			clientOutputMap.get(session).flush();
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	@Override
	public void onWebSocketBinary(Session session, byte[] data, int offset, int length) {
	}

	@Override
	public void onWebSocketError(Session session, Throwable error) {
		synchronized (monitor) {
			remove(session);
		}
	}

	@Override
	public void onWebSocketClose(Session session, int statusCode, String reason) {
		synchronized (monitor) {
			remove(session);
		}
	}

	private void remove(Session session) {
		if (executorServiceMap.containsKey(session)) executorServiceMap.get(session).shutdown();
		executorServiceMap.remove(session);
		clientOutputMap.remove(session);
		serverInputMap.remove(session);
	}

	private void notificationThread(Session session) {
		try {
			if (!session.isOpen()) return;
			if (!serverInputMap.containsKey(session)) return;
			if (serverInputMap.get(session).available() < 0) return;
			int bytesRead;
			byte[] buffer = new byte[8096];
			while ((bytesRead = serverInputMap.get(session).read(buffer)) != -1) {
				String content = new String(buffer, 0, bytesRead);
				content = content.replaceAll("Content-Length: [0-9+]*", "").trim();
				for (String message : content.split("\r?\n")) {
					if (message.isEmpty()) continue;
					session.getRemote().sendString(message.trim());
				}
			}
		} catch (IOException e) {
			if (session.isOpen()) Logger.error(e);
		}
	}
}
