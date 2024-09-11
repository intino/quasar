package io.intino.ime.box.models;

import io.intino.alexandria.logger.Logger;
import io.intino.ime.model.Model;
import org.apache.commons.io.IOUtils;
import org.eclipse.lsp4j.CreateFilesParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class ModelContainer {
	private final Model model;
	private final LanguageServer server;
	private static Map<String, String> LanguagesMap = null;

	public ModelContainer(Model model, LanguageServer server) {
		this.model = model;
		this.server = server;
		loadLanguages();
	}

	public List<File> files() {
		return new ModelContainerReader(model, server).files();
	}

	public File file(String name) {
		return files().stream().filter(f -> f.name().equals(name)).findFirst().orElse(null);
	}

	public static class File {
		private final String name;
		private final String uri;
		private final List<String> parents;

		public File(String name, String uri, List<String> parents) {
			this.name = name;
			this.uri = uri;
			this.parents = parents;
		}

		public String name() {
			return name;
		}

		public String extension() {
			return extensionOf(name);
		}

		public List<String> parents() {
			return parents;
		}

		public String uri() {
			return uri;
		}

		private static final String DefaultLanguage = "tara";
		public String language() {
			return LanguagesMap.getOrDefault(extensionOf(name), DefaultLanguage);
		}

		private static final String DefaultExtension = "tara";
		private String extensionOf(String name) {
			if (!name.contains(".")) return DefaultExtension;
			return name.substring(name.lastIndexOf(".")+1);
		}

		public boolean isDirectory() {
			if (!name.contains("/")) return !name.contains(".");
			String filename = name.substring(name.lastIndexOf("/"));
			return !filename.contains(".");
		}
	}

	private void loadLanguages() {
		try {
			if (LanguagesMap != null) return;
			InputStream stream = ModelContainer.class.getResourceAsStream("/programming-languages.tsv");
			if (stream == null) return;
			LanguagesMap = IOUtils.readLines(stream, StandardCharsets.UTF_8).stream().collect(Collectors.toMap(l -> l.split("\t")[0], l -> l.split("\t")[1]));
		}
		catch (Throwable e) {
			Logger.error(e);
		}
	}

}
