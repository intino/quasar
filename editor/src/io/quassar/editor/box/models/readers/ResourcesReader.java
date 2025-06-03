package io.quassar.editor.box.models.readers;

import io.intino.alexandria.logger.Logger;
import io.quassar.editor.box.models.FileReader;
import io.quassar.editor.box.models.Workspace;
import io.quassar.editor.box.util.WorkspaceHelper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class ResourcesReader implements FileReader {
	private final Workspace workspace;

	public ResourcesReader(Workspace workspace) {
		this.workspace = workspace;
	}

	@Override
	public List<io.quassar.editor.box.models.File> files() {
		File resourcesDirectory = new File(workspace.root(), io.quassar.editor.box.models.File.ResourcesDirectory);
		if (!resourcesDirectory.exists()) return Collections.emptyList();
		try(Stream<Path> paths = Files.walk(resourcesDirectory.toPath())) {
			return paths.map(p -> WorkspaceHelper.fileOf(p, workspace)).toList();
		} catch (IOException e) {
			Logger.error(e);
			return Collections.emptyList();
		}
	}

	@Override
	public boolean exists(String name, io.quassar.editor.box.models.File parent) {
		return new File(location(parent), name).exists();
	}

	@Override
	public boolean exists(io.quassar.editor.box.models.File file) {
		return new File(workspace.root(), file.uri()).exists();
	}

	@Override
	public io.quassar.editor.box.models.File get(String uri) {
		return WorkspaceHelper.fileOf(new File(workspace.root(), uri), workspace);
	}

	@Override
	public InputStream content(String uri) {
		try {
			return new FileInputStream(new File(workspace.root(), uri));
		} catch (FileNotFoundException e) {
			Logger.error(e);
			return null;
		}
	}

	private List<io.quassar.editor.box.models.File> filter(List<io.quassar.editor.box.models.File> files) {
		return files.stream().filter(io.quassar.editor.box.models.File::isResource).toList();
	}

	private java.io.File location(io.quassar.editor.box.models.File file) {
		if (file == null) return workspace.root();
		return new java.io.File(workspace.root(), file.uri());
	}
}
