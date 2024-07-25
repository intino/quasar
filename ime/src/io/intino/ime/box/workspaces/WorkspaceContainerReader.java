package io.intino.ime.box.workspaces;

import io.intino.alexandria.logger.Logger;
import io.intino.ime.box.util.WorkspaceHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WorkspaceContainerReader {
	private final File root;

	public WorkspaceContainerReader(File root) {
		this.root = root;
	}

	public WorkspaceContainer read() {
		WorkspaceContainer result = new WorkspaceContainer();
		if (!root.exists()) return result;
		try (Stream<Path> walk = Files.walk(root.toPath())) {
			register(walk.filter(f -> !f.toFile().getAbsolutePath().equals(root.getAbsolutePath()) && !f.toFile().getName().equals(".DS_Store")).collect(Collectors.toList()), result);
		} catch (IOException e) {
			Logger.error(e);
		}
		return result;
	}

	private void register(List<Path> pathList, WorkspaceContainer structure) {
		structure.add(pathList.stream().map(p -> WorkspaceHelper.fileOf(root, p)).toList());
	}

}
