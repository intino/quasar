package io.quassar.editor.box.models.writers;

import io.intino.alexandria.logger.Logger;
import io.quassar.editor.box.models.File;
import io.quassar.editor.box.models.FileWriter;
import io.quassar.editor.box.models.Workspace;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;

import static io.quassar.editor.box.util.WorkspaceHelper.relativePath;

public class ResourcesWriter implements FileWriter {
	private final Workspace workspace;

	public ResourcesWriter(Workspace workspace) {
		this.workspace = workspace;
	}

	@Override
	public File create(String filename, InputStream content, File parent) {
		try {
			java.io.File destiny = new java.io.File(location(parent), filename);
			FileUtils.copyInputStreamToFile(content, destiny);
			return new File(destiny.getName(), relativePath(destiny, workspace), false, new ArrayList<>());
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	@Override
	public File createFolder(String name, File parent) {
		java.io.File destiny = new java.io.File(location(parent), name);
		destiny.mkdirs();
		return new File(destiny.getName(), relativePath(destiny, workspace), false, new ArrayList<>());
	}

	@Override
	public void save(File file, InputStream content) {
		try {
			java.io.File destiny = location(file);
			FileUtils.copyInputStreamToFile(content, destiny);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	@Override
	public File rename(File file, String newName) {
		try {
			java.io.File source = location(file);
			java.io.File destiny = new java.io.File(location(file).getParentFile(), newName);
			Files.move(source.toPath(), destiny.toPath());
			return new File(destiny.getName(), relativePath(destiny, workspace), false, new ArrayList<>());
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	@Override
	public File move(File file, File directory) {
		try {
			java.io.File source = location(file);
			java.io.File destiny = location(directory);
			Files.move(source.toPath(), new java.io.File(destiny, file.name()).toPath());
			return new File(destiny.getName(), relativePath(destiny, workspace), false, new ArrayList<>());
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	@Override
	public File copy(String filename, File sourceFile) {
		try {
			java.io.File source = location(sourceFile);
			java.io.File destiny = new java.io.File(workspace.root(), filename);
			Files.copy(source.toPath(), destiny.toPath());
			return new File(destiny.getName(), relativePath(destiny, workspace), false, new ArrayList<>());
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	@Override
	public void remove(File file) {
		try {
			java.io.File source = location(file);
			if (file.isDirectory()) FileUtils.deleteDirectory(source);
			else source.delete();
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private java.io.File location(File file) {
		if (file == null) return workspace.root();
		return new java.io.File(workspace.root(), file.uri());
	}

}
