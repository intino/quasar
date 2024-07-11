import io.intino.ls.IntinoLanguageServer;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import tara.dsl.Proteo;

public class ServerTest {
	public static void main(String[] args) {
		IntinoLanguageServer server = new IntinoLanguageServer(new Proteo());
		Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, System.in, System.out);
		LanguageClient client = launcher.getRemoteProxy();
		server.connect(client);
		launcher.startListening();
	}
}
