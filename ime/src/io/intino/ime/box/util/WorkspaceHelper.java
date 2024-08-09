package io.intino.ime.box.util;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.workspaces.WorkspaceContainer;
import io.intino.ime.model.Language;
import io.intino.ime.model.Workspace;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class WorkspaceHelper {

	public static WorkspaceContainer.File fileOf(File rootContainer, Path path) {
		File file = path.toFile();
		String relativePath = file.getAbsolutePath().replace(rootContainer.getAbsolutePath(), "").replace("/" + file.getName(), "").replace(file.getName(), "");
		if (relativePath.startsWith("/")) relativePath = relativePath.substring(1);
		return new WorkspaceContainer.File(file.getName(), relativePath.isEmpty() ? Collections.emptyList() : List.of(relativePath.split("/")), file);
	}

	public static String proposeName() {
		String uuid = UUID.randomUUID().toString();
		return uuid.substring(uuid.lastIndexOf("-")+1) + new Timetag(Instant.now(), Scale.Month).value();
	}

	public static boolean validName(String name) {
		return StringUtils.isAlphanumeric(name);
	}

	public static boolean nameInUse(String value, ImeBox box) {
		return box.workspaceManager().exists(value);
	}

	public static Language language(Workspace workspace, ImeBox box) {
		return box.languageManager().get(workspace.language());
	}

}
