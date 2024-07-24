package io.intino.languageeditor.box.lsp;

import io.intino.alexandria.logger.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@WebSocket
public class LanguageServerWebSocketHandler {
	private LanguageServer server;
	private ExecutorService executorService;
	private PipedOutputStream clientOutput;
	private PipedInputStream serverInput;
	private final Object monitor = new Object();

	public void init(LanguageServer server) throws IOException {
		this.server = server;
	}

	@OnWebSocketConnect
	public void onConnect(Session session) {
		try {
			synchronized (monitor) {
				PipedInputStream clientInput = new PipedInputStream();
				clientOutput = new PipedOutputStream(clientInput);
				serverInput = new PipedInputStream();
				executorService = Executors.newSingleThreadExecutor();
				executorService.submit(() -> notificationThread(session));
				Launcher<LanguageClient> serverLauncher = LSPLauncher.createServerLauncher(server, clientInput, new PipedOutputStream(serverInput));
				serverLauncher.startListening();
			}
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	@OnWebSocketMessage
	public void onMessage(String message) {
		try {
			var content = "Content-Length: " + message.length() + "\n\n" + message;
			clientOutput.write(content.getBytes());
			clientOutput.flush();
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	@OnWebSocketClose
	public void onClose(int statusCode, String reason) {
		synchronized (monitor) {
			this.executorService.shutdown();
		}
	}

	private void notificationThread(Session session) {
		try {
			byte[] buffer = new byte[1024];
			while (serverInput.read(buffer) != -1) {
				String content = new String(buffer);
				content = content.substring(content.indexOf("\n"));
				session.getRemote().sendString(content.trim());
			}
		} catch (Throwable e) {
			Logger.error(e);
		}
	}
}
