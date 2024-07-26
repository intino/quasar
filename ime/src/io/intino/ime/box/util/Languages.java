package io.intino.ime.box.util;

import io.intino.alexandria.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Languages {
	private static volatile List<String> records = new ArrayList<>();

	public static void init(File file) {
		load(file);
	}

	private static void load(File file) {
		try {
			records = Files.readAllLines(file.toPath()).stream()
					.filter(l -> !l.trim().isEmpty())
					.toList();
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public static List<String> all() {
		return records;
	}
}
