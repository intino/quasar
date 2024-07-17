package io.intino.languageeditor.box.workspaces;

import io.intino.alexandria.logger.Logger;
import io.intino.languageeditor.box.util.WorkspaceHelper;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class WorkspaceContainerWriter {
	private final File root;

	public WorkspaceContainerWriter(File root) {
		this.root = root;
	}

	public WorkspaceContainer.File create(String filename, WorkspaceContainer.File parent) {
		try {
			if (!root.exists()) root.mkdirs();
			File file = new File(parent != null && parent.content().isDirectory() ? parent.content() : root, filename);
			if (!filename.contains(".")) createDirectory(file);
			else createEmptyFile(file);
			return WorkspaceHelper.fileOf(root, file.toPath());
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
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

	private static void createDirectory(File file) {
		file.mkdir();
	}

	private static void createEmptyFile(File file) throws IOException {
		file.getParentFile().mkdirs();
		Files.writeString(file.toPath(), "");
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
