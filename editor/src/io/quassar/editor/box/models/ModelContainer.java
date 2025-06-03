package io.quassar.editor.box.models;

import io.quassar.editor.box.models.readers.ModelReader;
import io.quassar.editor.box.models.readers.ResourcesReader;
import io.quassar.editor.model.FilePosition;
import io.quassar.editor.model.TokenLocation;
import org.eclipse.lsp4j.services.LanguageServer;

import java.util.List;

public class ModelContainer {
	private final Workspace workspace;
	private final LanguageServer server;

	public ModelContainer(Workspace workspace, LanguageServer server) {
		this.workspace = workspace;
		this.server = server;
	}

	public List<File> files() {
		return new WorkspaceReader(workspace, server).files();
	}

	public List<File> modelFiles() {
		return new WorkspaceReader(workspace, server).modelFiles();
	}

	public List<File> resourceFiles() {
		return new WorkspaceReader(workspace, server).resourceFiles();
	}

	public File file(String uri) {
		return File.isResource(uri) ? new ResourcesReader(workspace).get(uri) : new ModelReader(workspace, server).get(uri);
	}

	public boolean exists(File file) {
		return file.isResource() ? new ResourcesReader(workspace).exists(file) : new ModelReader(workspace, server).exists(file);
	}

}
