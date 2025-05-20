package io.intino.test;

import io.intino.ls.document.FileDocumentManager;
import io.intino.ls.IntinoLanguageServer;
import org.antlr.v4.runtime.misc.Pair;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.jsonrpc.json.StreamMessageProducer;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;
import org.eclipse.lsp4j.jsonrpc.util.ToStringBuilder;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.junit.*;
import tara.dsl.Metta;

import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Ignore
public class LauncherTest {

	private static final long TIMEOUT = 2000;

	private IntinoLanguageServer server;
	private Launcher<LanguageClient> serverLauncher;
	private Future<?> serverListening;

	private ClientEndpoint client;
	private Launcher<LanguageServer> clientLauncher;
	private Future<?> clientListening;

	private Level logLevel;

	@Before
	public void setup() throws IOException {
		PipedInputStream inClient = new PipedInputStream();
		PipedOutputStream outClient = new PipedOutputStream();
		PipedInputStream inServer = new PipedInputStream();
		PipedOutputStream outServer = new PipedOutputStream();
		File workspace = new File("./workspace");
		inClient.connect(outServer);
		outClient.connect(inServer);
		server = new IntinoLanguageServer(new Metta(), new FileDocumentManager(workspace));
		serverLauncher = LSPLauncher.createServerLauncher(server, inServer, outServer);
		serverListening = serverLauncher.startListening();

		client = new ClientEndpoint();
		clientLauncher = LSPLauncher.createClientLauncher(ServiceEndpoints.toServiceObject(client, LanguageClient.class), inClient, outClient);
		clientListening = clientLauncher.startListening();

		Logger logger = Logger.getLogger(StreamMessageProducer.class.getName());
		logLevel = logger.getLevel();
		logger.setLevel(Level.SEVERE);
	}

	@Test
	public void testNotification() {
		MessageParams p = new MessageParams();
		p.setMessage("Hello World");
		p.setType(MessageType.Info);
		client.expectedNotifications.put("window/logMessage", p);
		serverLauncher.getRemoteProxy().logMessage(p);
		client.joinOnEmpty();
	}

	@Test
	public void testHighlight() throws Exception {
		InitializeResult result = clientLauncher.getRemoteProxy().initialize(new InitializeParams()).get();
		clientLauncher.getRemoteProxy().getTextDocumentService().documentHighlight(new DocumentHighlightParams(new TextDocumentIdentifier("test/foo.tara"), new Position()));
//		Assert.assertEquals(Either.forRight(expected).toString(), future.get(TIMEOUT, TimeUnit.MILLISECONDS).toString());
		client.joinOnEmpty();

	}

	@Test
	public void testCompletion() throws Exception {
		clientLauncher.getRemoteProxy().initialize(new InitializeParams()).get();
		CompletionParams p = new CompletionParams();
		p.setPosition(new Position(1, 1));
		p.setTextDocument(new TextDocumentIdentifier("test/foo.txt"));

		CompletionItem item = new CompletionItem();
		item.setLabel("exampleCompletion");
		item.setInsertText("Example insert text");
		CompletionList expected = new CompletionList();
		expected.setIsIncomplete(false);
		expected.setItems(Arrays.asList(item));

		server.expectedRequests.put("textDocument/completion", new Pair<>(p, expected));
		CompletableFuture<Either<List<CompletionItem>, CompletionList>> future = clientLauncher.getRemoteProxy().getTextDocumentService().completion(p);
		Assert.assertEquals(Either.forRight(expected).toString(), future.get(TIMEOUT, TimeUnit.MILLISECONDS).toString());
		client.joinOnEmpty();
	}


	static class ClientEndpoint implements Endpoint {
		private final Map<String, Pair<Object, Object>> expectedRequests = new LinkedHashMap<>();
		private final Map<String, Object> expectedNotifications = new LinkedHashMap<>();

		@Override
		public CompletableFuture<?> request(String method, Object parameter) {
			Assert.assertTrue(expectedRequests.containsKey(method));
			Pair<Object, Object> result = expectedRequests.remove(method);
			Assert.assertEquals(result.a.toString(), parameter.toString());
			return CompletableFuture.completedFuture(result.b);
		}

		@Override
		public void notify(String method, Object parameter) {
			Assert.assertTrue(expectedNotifications.containsKey(method));
			Object object = expectedNotifications.remove(method);
			Assert.assertEquals(object.toString(), parameter.toString());
		}

		public void joinOnEmpty() {
			long before = System.currentTimeMillis();
			do {
				if (expectedNotifications.isEmpty() && expectedNotifications.isEmpty()) return;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} while (System.currentTimeMillis() < before + 1000);
			Assert.fail("expectations weren't empty " + toString());
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this).addAllFields().toString();
		}

	}


	@After
	public void teardown() throws InterruptedException {
		clientListening.cancel(true);
		serverListening.cancel(true);
		Thread.sleep(10);
		Logger logger = Logger.getLogger(StreamMessageProducer.class.getName());
		logger.setLevel(logLevel);
	}

}
