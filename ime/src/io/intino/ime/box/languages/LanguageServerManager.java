package io.intino.ime.box.languages;

import io.intino.ime.model.Model;
import io.intino.ls.IntinoLanguageServer;
import io.intino.ls.DocumentManager;
import io.intino.tara.Language;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class LanguageServerManager {
	private final LanguageLoader languageLoader;
	private final Map<String, LanguageServer> servers = new HashMap<>();

	public LanguageServerManager(LanguageLoader languageLoader) {
		this.languageLoader = languageLoader;
	}

	public LanguageServer create(Language language, URI workspaceRoot) throws IOException {
		return new IntinoLanguageServer(language, new DocumentManager(new File(workspaceRoot)));
	}

	public LanguageServer get(Model model) throws IOException {
		if (!servers.containsKey(model.name()))
			servers.put(model.name(), create(languageLoader.get(model.language()), model.documentRoot()));
		return servers.get(model.name());
	}
}