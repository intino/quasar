package io.quassar.editor.box.models;

import io.intino.alexandria.logger.Logger;
import io.quassar.editor.box.models.writers.ModelWriter;
import io.quassar.editor.box.models.writers.ResourcesWriter;
import io.quassar.editor.box.util.WorkspaceHelper;
import io.quassar.editor.model.Model;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class WorkspaceWriter {
	private final Workspace workspace;
	private final LanguageServer server;

	public WorkspaceWriter(Workspace workspace, LanguageServer server) {
		this.workspace = workspace;
		this.server = server;
	}

	public void clone(Model destinyModel, LanguageServer destinyServer) {
		try {
			if (destinyServer == null) return;
			Either<List<? extends SymbolInformation>, List<? extends WorkspaceSymbol>> symbols = server.getWorkspaceService().symbol(new WorkspaceSymbolParams()).get();
			List<io.quassar.editor.box.models.File> files = WorkspaceHelper.filesOf(symbols.getRight());
			WorkspaceWriter writer = new WorkspaceWriter(workspace.clone(destinyModel), destinyServer);
			files.stream().filter(f -> !f.isDirectory()).forEach(f -> writer.createFile(f.uri(), content(f.uri()), null));
		} catch (InterruptedException | ExecutionException e) {
			Logger.error(e);
		}
	}

	public io.quassar.editor.box.models.File createFile(String filename, InputStream content, io.quassar.editor.box.models.File parent) {
		return writer(filename).create(filename, content, parent);
	}

	public io.quassar.editor.box.models.File createFolder(String name, io.quassar.editor.box.models.File parent) {
		return writer(name).createFolder(name, parent);
	}

	public void save(io.quassar.editor.box.models.File file, InputStream content) {
		writer(file.uri()).save(file, content);
	}

	public io.quassar.editor.box.models.File rename(io.quassar.editor.box.models.File file, String newName) {
		return writer(file.uri()).rename(file, newName);
	}

	public io.quassar.editor.box.models.File move(io.quassar.editor.box.models.File file, io.quassar.editor.box.models.File directory) {
		return writer(file.uri()).move(file, directory);
	}

	public io.quassar.editor.box.models.File copy(String filename, io.quassar.editor.box.models.File source) {
		return writer(filename).copy(filename, source);
	}

	public void remove(io.quassar.editor.box.models.File file) {
		writer(file.uri()).remove(file);
	}

	private InputStream content(String uri) {
		return new WorkspaceReader(workspace, server).content(uri);
	}

	private FileWriter writer(String uri) {
		return io.quassar.editor.box.models.File.isResource(uri) ? new ResourcesWriter(workspace) : new ModelWriter(workspace, server);
	}

}
