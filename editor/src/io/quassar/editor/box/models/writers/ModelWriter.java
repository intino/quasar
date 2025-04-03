package io.quassar.editor.box.models.writers;

import io.intino.alexandria.logger.Logger;
import io.quassar.editor.box.models.File;
import io.quassar.editor.box.models.FileWriter;
import io.quassar.editor.box.models.Workspace;
import io.quassar.editor.box.models.WorkspaceReader;
import io.quassar.editor.box.util.WorkspaceHelper;
import org.apache.commons.io.IOUtils;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

public class ModelWriter implements FileWriter {
	private final Workspace workspace;
	private final LanguageServer server;

	public ModelWriter(Workspace workspace, LanguageServer server) {
		this.workspace = workspace;
		this.server = server;
	}

	@Override
	public File copy(String filename, File source) {
		String parent = WorkspaceHelper.parent(source.uri());
		String uri = (!parent.isEmpty() ? parent + java.io.File.separator : "") + filename;
		String content = content(source.uri());
		server.getWorkspaceService().didCreateFiles(new CreateFilesParams(List.of(new FileCreate(uri))));
		server.getTextDocumentService().didSave(new DidSaveTextDocumentParams(new TextDocumentIdentifier(uri), content));
		return new File(filename, uri, source.isDirectory(), new ArrayList<>());
	}

	@Override
	public File create(String filename, InputStream content, File parent) {
		String uri = (parent != null && parent.isDirectory() ? parent.uri() + java.io.File.separator : "") + filename;
		server.getWorkspaceService().didCreateFiles(new CreateFilesParams(List.of(new FileCreate(uri))));
		if (content != null) server.getTextDocumentService().didSave(new DidSaveTextDocumentParams(new TextDocumentIdentifier(uri), toString(content)));
		return new File(filename, uri, false, new ArrayList<>());
	}

	@Override
	public File createFolder(String name, File parent) {
		String uri = (parent != null && parent.isDirectory() ? parent.uri() + java.io.File.separator : "") + name;
		DidChangeWorkspaceFoldersParams params = new DidChangeWorkspaceFoldersParams(new WorkspaceFoldersChangeEvent(List.of(new WorkspaceFolder(uri, name)), emptyList()));
		server.getWorkspaceService().didChangeWorkspaceFolders(params);
		return new File(name, uri, true, new ArrayList<>());
	}

	@Override
	public void save(File file, InputStream content) {
		server.getTextDocumentService().didSave(new DidSaveTextDocumentParams(new TextDocumentIdentifier(file.uri()), toString(content)));
	}

	@Override
	public File rename(File file, String newName) {
		String parent = WorkspaceHelper.parent(file.uri());
		String newUri = (!parent.isEmpty() ? parent + java.io.File.separator : "") + newName;
		server.getWorkspaceService().didRenameFiles(new RenameFilesParams(List.of(new FileRename(file.uri(), newUri))));
		return new File(newName, newUri, file.isDirectory(), file.parents());
	}

	@Override
	public File move(File file, File directory) {
		String newUri = (directory != null ? directory.uri() + java.io.File.separator : "") + file.name();
		server.getWorkspaceService().didRenameFiles(new RenameFilesParams(List.of(new FileRename(file.uri(), newUri))));
		return new File(file.name(), newUri, file.isDirectory(), new ArrayList<>());
	}

	@Override
	public void remove(File file) {
		if (file.isDirectory()) {
			DidChangeWorkspaceFoldersParams params = new DidChangeWorkspaceFoldersParams(new WorkspaceFoldersChangeEvent(emptyList(), List.of(new WorkspaceFolder(file.uri(), file.name()))));
			server.getWorkspaceService().didChangeWorkspaceFolders(params);
		} else {
			DeleteFilesParams params = new DeleteFilesParams(List.of(new FileDelete(file.uri())));
			server.getWorkspaceService().didDeleteFiles(params);
		}
	}

	private String content(String uri) {
		try {
			InputStream content = new WorkspaceReader(workspace, server).content(uri);
			return content != null ? IOUtils.toString(content, StandardCharsets.UTF_8) : "";
		} catch (IOException e) {
			Logger.error(e);
			return "";
		}
	}

	private String toString(InputStream content) {
		try {
			return IOUtils.toString(content, StandardCharsets.UTF_8);
		} catch (IOException ignored) {
			return null;
		}
	}

}
