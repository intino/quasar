package io.quassar.editor.box.builder;

import java.util.List;

public class ArchetypeEntry {
	private final String path;
	private final List<String> builders;

	public ArchetypeEntry(String path, List<String> builders) {
		this.path = path;
		this.builders = builders;
	}

	public String path() {
		return path.contains("[") ? path.substring(0, path.indexOf("[")) : path;
	}

	public List<String> builders() {
		return builders;
	}

	@Override
	public String toString() {
		return path + " <= " + String.join(", ", builders);
	}
}

