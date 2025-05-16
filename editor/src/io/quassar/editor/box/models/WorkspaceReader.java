package io.quassar.editor.box.models;

import io.quassar.editor.box.models.readers.ModelReader;
import io.quassar.editor.box.models.readers.ResourcesReader;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class WorkspaceReader {
	private final Workspace workspace;
	private final LanguageServer server;

	public WorkspaceReader(Workspace workspace, LanguageServer server) {
		this.workspace = workspace;
		this.server = server;
	}

	public List<io.quassar.editor.box.models.File> files() {
		List<io.quassar.editor.box.models.File> result = new ArrayList<>();
		result.addAll(modelFiles());
		result.addAll(resourceFiles());
		return result;
	}

	public List<io.quassar.editor.box.models.File> modelFiles() {
		return new ModelReader(workspace, server).files();
	}

	public List<io.quassar.editor.box.models.File> resourceFiles() {
		return new ResourcesReader(workspace).files();
	}

	public boolean existsFile(String filename, io.quassar.editor.box.models.File parent) {
		return readerOf(parent != null ? parent.uri() : filename).exists(filename, parent);
	}

	public InputStream content(String uri) {
		return readerOf(uri).content(uri);
	}

	private FileReader readerOf(String uri) {
		return io.quassar.editor.box.models.File.isResource(uri) ? new ResourcesReader(workspace) : new ModelReader(workspace, server);
	}

}
