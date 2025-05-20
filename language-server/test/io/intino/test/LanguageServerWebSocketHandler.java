package io.intino.test;

import io.intino.alexandria.logger.Logger;
import io.intino.ls.IntinoLanguageServer;
import io.intino.ls.document.FileDocumentManager;
import io.intino.tara.Tara;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import tara.dsl.Metta;

import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.ByteBuffer.wrap;

@WebSocket
public class LanguageServerWebSocketHandler {
	private final LanguageServer server;
	private Session session;
	private final ExecutorService executorService = Executors.newCachedThreadPool();
	private PipedInputStream input;
	private PipedOutputStream output;

	public static void main(String[] args) throws Exception {
		Server server = new Server(8081);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);
		JettyWebSocketServletContainerInitializer.configure(context, (servletContext, wsContainer) -> {
			wsContainer.addMapping("/ws", (req, resp) -> {
				try {
					return new LanguageServerWebSocketHandler(new Metta(), new File("./workspace"));
				} catch (IOException e) {
					Logger.error(e);
					return null;
				}
			});
		});
		server.setHandler(context);
		server.start();
		server.join();
	}

	public LanguageServerWebSocketHandler(Tara dsl, File workspace) throws IOException {
		server = new IntinoLanguageServer(dsl, new FileDocumentManager(workspace));
	}

	@OnWebSocketConnect
	public void onConnect(Session session) {
		this.session = session;
		try {
			input = new PipedInputStream();
			output = new PipedOutputStream(input);
			this.executorService.submit(this::notificationThread);
			Launcher<LanguageClient> serverLauncher = LSPLauncher.createServerLauncher(server, input, output);
			serverLauncher.startListening();
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	private void notificationThread() {
		try {
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = input.read(buffer)) != -1)
				this.session.getRemote().sendBytes(wrap(buffer, 0, bytesRead));
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
}
