package io.intino.ime.box.models;

import io.intino.alexandria.logger.Logger;
import io.intino.ime.box.util.ModelHelper;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ModelContainerWriter {
	private final File root;

	public ModelContainerWriter(File root) {
		this.root = root;
	}

	public void clone(File destiny) {
		try {
			FileUtils.copyDirectory(root, destiny);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public ModelContainer.File copy(String filename, ModelContainer.File source) {
		try {
			if (!root.exists()) root.mkdirs();
			File file = new File(source.content().getParentFile(), filename);
			if (source.content().isDirectory()) createDirectory(file);
			else createFile(file, Files.readString(source.content().toPath()));
			return ModelHelper.fileOf(root, file.toPath());
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	public ModelContainer.File createFile(String filename, String content, ModelContainer.File parent) {
		try {
			if (!root.exists()) root.mkdirs();
			File file = new File(parent != null && parent.content().isDirectory() ? parent.content() : root, filename);
			createFile(file, content);
			return ModelHelper.fileOf(root, file.toPath());
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	public ModelContainer.File createFolder(String name, ModelContainer.File parent) {
		if (!root.exists()) root.mkdirs();
		File file = new File(parent != null && parent.content().isDirectory() ? parent.content() : root, name);
		createDirectory(file);
		return ModelHelper.fileOf(root, file.toPath());
	}

	public void save(ModelContainer.File file, String content) {
		try {
			if (!root.exists()) root.mkdirs();
			File destination = file.content();
			Files.writeString(destination.toPath(), content);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public ModelContainer.File rename(ModelContainer.File file, String newName) {
		File systemFile = file.content();
		File destiny = new File(systemFile.getParentFile(), newName);
		systemFile.renameTo(destiny);
		return ModelHelper.fileOf(root, destiny.toPath());
	}

	public ModelContainer.File move(ModelContainer.File file, ModelContainer.File directory) {
		File systemFile = file.content();
		File systemDirectory = directory != null ? directory.content() : root;
		File destiny = new File(systemDirectory, systemFile.getName());
		systemFile.renameTo(destiny);
		return ModelHelper.fileOf(root, destiny.toPath());
	}

	private static void createDirectory(File file) {
		file.mkdir();
	}

	private static void createFile(File file, String content) throws IOException {
		file.getParentFile().mkdirs();
		Files.writeString(file.toPath(), content);
	}

	public void remove(ModelContainer.File file) {
		try {
			File content = file.content();
			if (!content.exists()) return;
			if (content.isDirectory()) FileUtils.deleteDirectory(content);
			else file.content().delete();
		} catch (IOException e) {
			Logger.error(e);
		}
	}

}
