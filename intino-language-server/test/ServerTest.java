import io.intino.ls.IntinoLanguageServer;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.junit.Test;

public class ServerTest {

	@Test
	public void should_connect() {
// Inicia el servidor en System.in y System.out
		IntinoLanguageServer server = new IntinoLanguageServer();
		Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, System.in, System.out);
		LanguageClient client = launcher.getRemoteProxy();
		launcher.startListening();
	}
}
