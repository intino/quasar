package io.intino.ime.box.util;

import io.intino.ime.box.workspaces.WorkspaceContainer;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class WorkspaceHelper {

	public static WorkspaceContainer.File fileOf(File rootContainer, Path path) {
		File file = path.toFile();
		String relativePath = file.getAbsolutePath().replace(rootContainer.getAbsolutePath(), "").replace("/" + file.getName(), "").replace(file.getName(), "");
		if (relativePath.startsWith("/")) relativePath = relativePath.substring(1);
		return new WorkspaceContainer.File(file.getName(), relativePath.isEmpty() ? Collections.emptyList() : List.of(relativePath.split("/")), file);
	}

}
