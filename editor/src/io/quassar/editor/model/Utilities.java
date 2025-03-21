package io.quassar.editor.model;

import io.intino.alexandria.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Utilities {
	private Set<String> records = new HashSet<>();
	private final File file;

	public Utilities(File file) {
		this.file = file;
		load();
	}

	public List<String> all() {
		return new ArrayList<>(records);
	}

	public void add(String utility) {
		records.add(utility);
		save();
	}

	private void save() {
		try {
			Files.writeString(file.toPath(), String.join("\n", records));
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private void load() {
		try {
			if (!file.exists()) return;
			records = new HashSet<>(Files.readAllLines(file.toPath()));
		} catch (IOException e) {
			Logger.error(e);
		}
	}
}
