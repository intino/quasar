package io.intino.languageeditor.box.lsp;

import io.intino.alexandria.logger.Logger;
import io.intino.ls.DocumentManager;
import io.intino.ls.IntinoLanguageServer;
import io.intino.tara.Tara;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import tara.dsl.Proteo;

import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.ByteBuffer.wrap;

@WebSocket
public class LanguageServerWebSocketHandler {
	private PipedInputStream input;
	private PipedOutputStream output;
	private LanguageServer server;
	private final ExecutorService executorService = Executors.newCachedThreadPool();

	public LanguageServerWebSocketHandler() {
	}

	public void init(LanguageServer server) throws IOException {
		this.server = server;
	}

	@OnWebSocketConnect
	public void onConnect(Session session) {
		try {
			input = new PipedInputStream();
			output = new PipedOutputStream(input);
			executorService.submit(() -> notificationThread(session));
			Launcher<LanguageClient> serverLauncher = LSPLauncher.createServerLauncher(server, input, output);
			serverLauncher.startListening();
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	@OnWebSocketMessage
	public void onMessage(String message) {
		try {
			output.write(message.getBytes());
			output.flush();
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	@OnWebSocketClose
	public void onClose(int statusCode, String reason) {
		this.executorService.shutdown();
	}

	private void notificationThread(Session session) {
		try {
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = input.read(buffer)) != -1)
				session.getRemote().sendBytes(wrap(buffer, 0, bytesRead));
		} catch (Exception e) {
			Logger.error(e);
		}
	}
}
