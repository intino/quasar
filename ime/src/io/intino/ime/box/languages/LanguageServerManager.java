package io.intino.ime.box.languages;

import io.intino.ime.model.Model;
import io.intino.ls.DocumentManager;
import io.intino.ls.IntinoLanguageServer;
import io.intino.tara.Language;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class LanguageServerManager {
	private final LanguageLoader languageLoader;
	private final Function<Model, URI> workspaceProvider;
	private final Map<String, LanguageServer> servers = new HashMap<>();

	public LanguageServerManager(LanguageLoader languageLoader, Function<Model, URI> workspaceProvider) {
		this.languageLoader = languageLoader;
		this.workspaceProvider = workspaceProvider;
	}

	public LanguageServer create(Language language, URI workspaceRoot) throws IOException {
		File root = new File(workspaceRoot);
		root.mkdirs();
		return new IntinoLanguageServer(language, new DocumentManager(root));
	}

	public LanguageServer get(Model model) throws IOException {
		if (!servers.containsKey(model.id()))
			servers.put(model.id(), create(languageLoader.get(model.language()), workspaceProvider.apply(model)));
		return servers.get(model.id());
	}
}