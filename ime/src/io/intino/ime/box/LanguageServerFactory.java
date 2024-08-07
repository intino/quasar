package io.intino.ime.box;

import io.intino.ime.model.Workspace;
import io.intino.ls.IntinoLanguageServer;
import io.intino.ls.WorkspaceManager;
import io.intino.tara.Language;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class LanguageServerFactory {
	private final LanguageProvider languageProvider;
	private final Map<String, LanguageServer> servers = new HashMap<>();

	public LanguageServerFactory(LanguageProvider languageProvider) {
		this.languageProvider = languageProvider;
	}

	public LanguageServer create(Language language, URI workspaceRoot) throws IOException {
		return new IntinoLanguageServer(language, new WorkspaceManager(new File(workspaceRoot)));
	}

	public LanguageServer get(Workspace workspace) throws IOException {
		if (!servers.containsKey(workspace.name()))
			servers.put(workspace.name(), create(languageProvider.get(workspace.language()), workspace.documentRoot()));
		return servers.get(workspace.name());
	}
}
