package io.quassar.editor.box.models;

import io.intino.alexandria.logger.Logger;
import io.intino.ls.IntinoLanguageServer;
import io.quassar.editor.box.util.WorkspaceHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ModelContainerReader {
	private final Language language;
	private final Model model;
	private final LanguageServer server;

	public ModelContainerReader(Language language, Model model, LanguageServer server) {
		this.model = model;
		this.language = language;
		this.server = server;
	}

	public List<ModelContainer.File> files() {
		try {
			if (server == null) return Collections.emptyList();
			Either<List<? extends SymbolInformation>, List<? extends WorkspaceSymbol>> symbols = server.getWorkspaceService().symbol(new WorkspaceSymbolParams()).get();
			return WorkspaceHelper.filesOf(symbols.getRight());
		} catch (InterruptedException | ExecutionException e) {
			Logger.error(e);
			return Collections.emptyList();
		}
	}

	public List<ModelContainer.File> modelFiles() {
		return files().stream().filter(f -> f.name().contains(language.fileExtension()) || (f.isDirectory() && !isResourceFile(f))).toList();
	}

	public List<ModelContainer.File> resourceFiles() {
		return files().stream().filter(this::isResourceFile).toList();
	}

	public boolean existsFile(String filename, ModelContainer.File parent) {
		String uri = (parent != null && parent.isDirectory() ? parent.uri() + File.separator : "") + filename;
		return files().stream().anyMatch(f -> f.uri().contains(uri));
	}

	public boolean isResourceFile(ModelContainer.File file) {
		return Model.isResource(file.uri());
	}

	public String content(String uri) {
		if (server == null) return "";
		try(InputStream content = ((IntinoLanguageServer) server).getWorkspaceService().content(URI.create(uri))) {
			return new String(content.readAllBytes(), Charset.defaultCharset());
		} catch (Exception ignored) {
			return "";
		}
	}

}
