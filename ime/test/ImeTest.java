import io.intino.ime.box.orchestator.BuilderOrchestator;
import io.intino.ls.document.FileDocumentManager;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class ImeTest {

	public static void main(String[] args) throws IOException, URISyntaxException {
		final FileDocumentManager fileDocumentManager = new FileDocumentManager(new File("temp/projects/tafat"));
		new BuilderOrchestator(new URI("http://localhost:9000").toURL(), fileDocumentManager).build("");
	}
}
