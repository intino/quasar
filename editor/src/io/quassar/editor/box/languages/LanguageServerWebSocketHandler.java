package io.quassar.editor.box.languages;

import io.intino.alexandria.logger.Logger;
import io.intino.ls.IntinoLanguageServer;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class LanguageServerWebSocketHandler implements WebSocketListener {
	private final Function<Session, LanguageServer> provider;
	private ExecutorService executorService;
	private PipedOutputStream clientOutput;
	private PipedInputStream serverInput;
	private final Object monitor = new Object();

	public LanguageServerWebSocketHandler(Function<Session, LanguageServer> provider) {
		this.provider = provider;
	}

	@Override
	public void onWebSocketConnect(Session session) {
		try {
			synchronized (monitor) {
				session.setIdleTimeout(Duration.ofHours(1));
				PipedInputStream clientInput = new PipedInputStream();
				clientOutput = new PipedOutputStream(clientInput);
				serverInput = new PipedInputStream();
				PipedOutputStream out = new PipedOutputStream(serverInput);
				executorService = Executors.newSingleThreadExecutor();
				executorService.submit(() -> notificationThread(session));
				IntinoLanguageServer server = (IntinoLanguageServer) provider.apply(session);
				Launcher<LanguageClient> serverLauncher = LSPLauncher.createServerLauncher(server, clientInput, out);
				server.connect(serverLauncher.getRemoteProxy());
				serverLauncher.startListening();
			}
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	@Override
	public void onWebSocketText(String message) {
		try {
			var content = "Content-Length: " + message.length() + "\n\n" + message;
			clientOutput.write(content.getBytes());
			clientOutput.flush();
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		synchronized (monitor) {
			this.executorService.shutdown();
		}
	}

	private void notificationThread(Session session) {
		try {
			int bytesRead;
			byte[] buffer = new byte[2048];
			while ((bytesRead = serverInput.read(buffer)) != -1) {
				String content = new String(buffer, 0, bytesRead);
				if (content.contains("\n"))
					content = content.substring(content.indexOf("\n"));
				session.getRemote().sendString(content.trim());
			}
		} catch (Throwable e) {
			Logger.error(e);
		}
	}
}
