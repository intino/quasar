package io.intino.ime.box;

import io.intino.ls.IntinoLanguageServer;
import io.intino.ls.WorkspaceManager;
import io.intino.tara.Tara;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class LanguageServerFactory {


	public LanguageServer create(Tara language, URI workspaceRoot) throws IOException {
		return new IntinoLanguageServer(language, new WorkspaceManager(new File(workspaceRoot)));
	}
}
