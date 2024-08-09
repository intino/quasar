package io.intino.ime.box.workspaces;

import io.intino.alexandria.logger.Logger;
import io.intino.ime.box.util.WorkspaceHelper;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class WorkspaceContainerWriter {
	private final File root;

	public WorkspaceContainerWriter(File root) {
		this.root = root;
	}

	public void clone(File destiny) {
		try {
			FileUtils.copyDirectory(root, destiny);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public WorkspaceContainer.File copy(String filename, WorkspaceContainer.File source) {
		try {
			if (!root.exists()) root.mkdirs();
			File file = new File(source.content().getParentFile(), filename);
			if (source.content().isDirectory()) createDirectory(file);
			else createFile(file, Files.readString(source.content().toPath()));
			return WorkspaceHelper.fileOf(root, file.toPath());
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	public WorkspaceContainer.File createFile(String filename, String content, WorkspaceContainer.File parent) {
		try {
			if (!root.exists()) root.mkdirs();
			File file = new File(parent != null && parent.content().isDirectory() ? parent.content() : root, filename);
			createFile(file, content);
			return WorkspaceHelper.fileOf(root, file.toPath());
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	public WorkspaceContainer.File createFolder(String name, WorkspaceContainer.File parent) {
		if (!root.exists()) root.mkdirs();
		File file = new File(parent != null && parent.content().isDirectory() ? parent.content() : root, name);
		createDirectory(file);
		return WorkspaceHelper.fileOf(root, file.toPath());
	}

	public void save(WorkspaceContainer.File file, String content) {
		try {
			if (!root.exists()) root.mkdirs();
			File destination = file.content();
			Files.writeString(destination.toPath(), content);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public WorkspaceContainer.File rename(WorkspaceContainer.File file, String newName) {
		File systemFile = file.content();
		File destiny = new File(systemFile.getParentFile(), newName);
		systemFile.renameTo(destiny);
		return WorkspaceHelper.fileOf(root, destiny.toPath());
	}

	public WorkspaceContainer.File move(WorkspaceContainer.File file, WorkspaceContainer.File directory) {
		File systemFile = file.content();
		File systemDirectory = directory != null ? directory.content() : root;
		File destiny = new File(systemDirectory, systemFile.getName());
		systemFile.renameTo(destiny);
		return WorkspaceHelper.fileOf(root, destiny.toPath());
	}

	private static void createDirectory(File file) {
		file.mkdir();
	}

	private static void createFile(File file, String content) throws IOException {
		file.getParentFile().mkdirs();
		Files.writeString(file.toPath(), content);
	}

	public void remove(WorkspaceContainer.File file) {
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
