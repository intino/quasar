package io.quassar.editor.box.languages;

import io.intino.ls.IntinoDocumentService;
import io.intino.ls.IntinoLanguageServer;
import io.intino.ls.document.DocumentManager;
import io.intino.ls.document.FileDocumentManager;
import io.intino.ls.document.GitDocumentManager;
import io.intino.tara.Language;
import io.quassar.editor.model.Model;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class LanguageServerManager {
	private final LanguageLoader languageLoader;
	private final BiFunction<Model, String, URI> workspaceProvider;
	private final Map<String, LanguageServer> servers = new HashMap<>();
	private Consumer<Model> changeWorkspaceListener;

	public LanguageServerManager(LanguageLoader languageLoader, BiFunction<Model, String, URI> workspaceProvider) {
		this.languageLoader = languageLoader;
		this.workspaceProvider = workspaceProvider;
	}

	public LanguageServerManager onChangeWorkspace(Consumer<Model> listener) {
		this.changeWorkspaceListener = listener;
		return this;
	}

	public LanguageServer create(Language language, URI workspaceRoot) throws IOException {
		File root = new File(workspaceRoot);
		root.mkdirs();
		return new IntinoLanguageServer(language, new FileDocumentManager(root));
	}

	public LanguageServer create(Language language, URI workspaceRoot, URL gitUrl, String branch, String username, String token) throws IOException, GitAPIException, URISyntaxException {
		File root = new File(workspaceRoot);
		root.mkdirs();
		return new IntinoLanguageServer(language, new GitDocumentManager(root, branch, gitUrl, username, token));
	}

	public LanguageServer get(Model model, String release) throws IOException, GitAPIException, URISyntaxException {
		String key = key(model, release);
		if (!servers.containsKey(key))
			servers.put(key, create(model, release));
		return servers.get(key);
	}

	public void remove(Model model, String release) {
		servers.remove(key(model, release));
		languageLoader.remove(model.language());
	}

	private String key(Model model, String release) {
		return model.id() + release;
	}

	private LanguageServer create(Model model, String release) throws IOException {
		Language language = languageLoader.get(model.language());
		URI workspace = workspaceProvider.apply(model, release);
		return withListeners(model, create(language, workspace));
	}

	private URL urlOf(String value) {
		try {
			if (value == null) return null;
			return new URI(value).toURL();
		} catch (MalformedURLException | URISyntaxException e) {
			return null;
		}
	}

	private LanguageServer withListeners(Model model, LanguageServer server) {
		((IntinoDocumentService)server.getTextDocumentService()).onChange(e -> changeWorkspaceListener.accept(model));
		return server;
	}

}