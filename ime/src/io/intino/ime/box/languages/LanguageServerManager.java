package io.intino.ime.box.languages;

import io.intino.ime.model.Model;
import io.intino.ls.IntinoLanguageServer;
import io.intino.ls.document.FileDocumentManager;
import io.intino.ls.document.GitDocumentManager;
import io.intino.tara.Language;
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
import java.util.function.Function;

public class LanguageServerManager {
	private final LanguageLoader languageLoader;
	private final Function<Model, URI> workspaceProvider;
	private final Function<String, String> gitAccessTokenProvider;
	private final Map<String, LanguageServer> servers = new HashMap<>();

	public LanguageServerManager(LanguageLoader languageLoader, Function<Model, URI> workspaceProvider, Function<String, String> gitAccessTokenProvider) {
		this.languageLoader = languageLoader;
		this.workspaceProvider = workspaceProvider;
		this.gitAccessTokenProvider = gitAccessTokenProvider;
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

	public LanguageServer get(Model model) throws IOException, GitAPIException, URISyntaxException {
		if (!servers.containsKey(model.modelingLanguage()))
			servers.put(model.modelingLanguage(), create(model));
		return servers.get(model.modelingLanguage());
	}

	public void remove(Model model) {
		servers.remove(model.modelingLanguage());
	}

	private LanguageServer create(Model model) throws IOException, GitAPIException, URISyntaxException {
		Language language = languageLoader.get(model.modelingLanguage());
		URI workspace = workspaceProvider.apply(model);
		URL url = urlOf(model.gitSettings().url());
		String branch = model.gitSettings().branch();
		String token = gitAccessTokenProvider.apply(model.owner());
		boolean hasGit = token != null && url != null && branch != null;
		return hasGit ? create(language, workspace, url, branch, model.owner(), token) : create(language, workspace);
	}

	private URL urlOf(String value) {
		try {
			if (value == null) return null;
			return new URI(value).toURL();
		} catch (MalformedURLException | URISyntaxException e) {
			return null;
		}
	}

}