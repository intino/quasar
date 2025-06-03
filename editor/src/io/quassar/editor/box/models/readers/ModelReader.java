package io.quassar.editor.box.models.readers;

import io.intino.alexandria.logger.Logger;
import io.intino.ls.IntinoLanguageServer;
import io.quassar.editor.box.models.File;
import io.quassar.editor.box.models.FileReader;
import io.quassar.editor.box.models.Workspace;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.box.util.WorkspaceHelper;
import io.quassar.editor.model.FilePosition;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.TokenLocation;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageServer;
import org.scribe.model.Token;

import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ModelReader implements FileReader {
	private final Workspace workspace;
	private final LanguageServer server;

	public ModelReader(Workspace workspace, LanguageServer server) {
		this.workspace = workspace;
		this.server = server;
	}

	public List<File> files() {
		try {
			if (server == null) return Collections.emptyList();
			Either<List<? extends SymbolInformation>, List<? extends WorkspaceSymbol>> symbols = server.getWorkspaceService().symbol(new WorkspaceSymbolParams()).get();
			return filter(WorkspaceHelper.filesOf(symbols.getRight()));
		} catch (InterruptedException | ExecutionException e) {
			Logger.error(e);
			return Collections.emptyList();
		}
	}

	@Override
	public boolean exists(String name, io.quassar.editor.box.models.File parent) {
		return new java.io.File(location(parent), name).exists();
	}

	@Override
	public boolean exists(io.quassar.editor.box.models.File file) {
		return new java.io.File(workspace.root(), file.uri()).exists();
	}

	@Override
	public io.quassar.editor.box.models.File get(String uri) {
		return WorkspaceHelper.fileOf(new java.io.File(workspace.root(), uri), workspace);
	}

	@Override
	public InputStream content(String uri) {
		if (server == null) return null;
		try(InputStream content = ((IntinoLanguageServer) server).getWorkspaceService().content(URI.create(uri))) {
			return content;
		} catch (Exception ignored) {
			return null;
		}
	}

	private List<File> filter(List<File> files) {
		return files.stream().filter(f -> f.name().contains(Language.FileExtension) || (f.isDirectory() && !f.isResource())).toList();
	}

	private java.io.File location(io.quassar.editor.box.models.File file) {
		if (file == null) return workspace.root();
		return new java.io.File(workspace.root(), file.uri());
	}

}
