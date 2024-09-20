package io.intino.ime.box.models;

import io.intino.alexandria.logger.Logger;
import io.intino.ime.box.util.WorkspaceHelper;
import io.intino.ime.model.Model;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.util.Collections.emptyList;

public class ModelContainerWriter {
	private final Model model;
	private final LanguageServer server;

	public ModelContainerWriter(Model model, LanguageServer server) {
		this.model = model;
		this.server = server;
	}

	public void clone(Model destinyModel, LanguageServer destinyServer) {
		try {
			if (destinyServer == null) return;
			Either<List<? extends SymbolInformation>, List<? extends WorkspaceSymbol>> symbols = server.getWorkspaceService().symbol(new WorkspaceSymbolParams()).get();
			List<ModelContainer.File> files = WorkspaceHelper.filesOf(symbols.getRight());
			CreateFilesParams params = new CreateFilesParams();
			params.setFiles(files.stream().map(WorkspaceHelper::fileCreateOf).toList());
			destinyServer.getWorkspaceService().didCreateFiles(params);
			files.stream().filter(f -> !f.isDirectory()).forEach(f -> destinyServer.getTextDocumentService().didSave(new DidSaveTextDocumentParams(new TextDocumentIdentifier(f.uri()), content(f.uri()))));
		} catch (InterruptedException | ExecutionException e) {
			Logger.error(e);
		}
	}

	public ModelContainer.File copy(String filename, ModelContainer.File source) {
		String parent = WorkspaceHelper.parent(source.uri());
		String uri = (!parent.isEmpty() ? parent + "/" : "") + filename;
		String content = content(source.uri());
		server.getWorkspaceService().didCreateFiles(new CreateFilesParams(List.of(new FileCreate(uri))));
		server.getTextDocumentService().didSave(new DidSaveTextDocumentParams(new TextDocumentIdentifier(uri), content));
		return new ModelContainer.File(filename, uri, source.isDirectory(), new ArrayList<>());
	}

	public ModelContainer.File createFile(String filename, String content, ModelContainer.File parent) {
		String uri = (parent != null && parent.isDirectory() ? parent.uri() + "/" : "") + filename;
		server.getWorkspaceService().didCreateFiles(new CreateFilesParams(List.of(new FileCreate(uri))));
		if (content != null) server.getTextDocumentService().didSave(new DidSaveTextDocumentParams(new TextDocumentIdentifier(uri), content));
		return new ModelContainer.File(filename, uri, false, new ArrayList<>());
	}

	public ModelContainer.File createFolder(String name, ModelContainer.File parent) {
		String uri = (parent != null && parent.isDirectory() ? parent.uri() + "/" : "") + name;
		DidChangeWorkspaceFoldersParams params = new DidChangeWorkspaceFoldersParams(new WorkspaceFoldersChangeEvent(List.of(new WorkspaceFolder(uri, name)), emptyList()));
		server.getWorkspaceService().didChangeWorkspaceFolders(params);
		return new ModelContainer.File(name, uri, true, new ArrayList<>());
	}

	public void save(ModelContainer.File file, String content) {
		server.getTextDocumentService().didSave(new DidSaveTextDocumentParams(new TextDocumentIdentifier(file.uri()), content));
	}

	public ModelContainer.File rename(ModelContainer.File file, String newName) {
		String parent = WorkspaceHelper.parent(file.uri());
		String newUri = (!parent.isEmpty() ? parent + "/" : "") + newName;
		server.getWorkspaceService().didRenameFiles(new RenameFilesParams(List.of(new FileRename(file.uri(), newUri))));
		return new ModelContainer.File(newName, newUri, file.isDirectory(), file.parents());
	}

	public ModelContainer.File move(ModelContainer.File file, ModelContainer.File directory) {
		String newUri = directory.uri() + "/" + file.name();
		server.getWorkspaceService().didRenameFiles(new RenameFilesParams(List.of(new FileRename(file.uri(), newUri))));
		return new ModelContainer.File(file.name(), newUri, file.isDirectory(), new ArrayList<>());
	}

	private void createFile(ModelContainer.File file, String content) throws IOException {
		CreateFilesParams params = new CreateFilesParams(List.of(new FileCreate(file.uri())));
		server.getWorkspaceService().didCreateFiles(params);
		server.getTextDocumentService().didSave(new DidSaveTextDocumentParams(new TextDocumentIdentifier(file.uri()), content));
	}

	public void remove(ModelContainer.File file) {
		if (file.isDirectory()) {
			DidChangeWorkspaceFoldersParams params = new DidChangeWorkspaceFoldersParams(new WorkspaceFoldersChangeEvent(emptyList(), List.of(new WorkspaceFolder(file.uri(), file.name()))));
			server.getWorkspaceService().didChangeWorkspaceFolders(params);
		} else {
			DeleteFilesParams params = new DeleteFilesParams(List.of(new FileDelete(file.uri())));
			server.getWorkspaceService().didDeleteFiles(params);
		}
	}

	private String content(String uri) {
		return new ModelContainerReader(model, server).content(uri);
	}

}
