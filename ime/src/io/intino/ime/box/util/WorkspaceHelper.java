package io.intino.ime.box.util;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.workspaces.WorkspaceContainer;

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

	public static String proposeName(ImeBox box) {
		return sequence(Instant.now(), WorkspaceSequence.get()+1);
	}

	public static String sequence(Timetag timetag, long value) {
		return sequence(new Timetag(timetag.datetime(), Scale.Year), Formatters.padded(value, 10));
	}

	public static String sequence(Instant instant, long value) {
		return sequence(new Timetag(instant, Scale.Year), Formatters.padded(value, 10));
	}

	public static String sequence(Timetag timetag, String value) {
		return new Timetag(timetag.datetime(), Scale.Year) + value;
	}

	public static String sequence(Instant instant, String value) {
		return new Timetag(instant, Scale.Year) + value;
	}


}
