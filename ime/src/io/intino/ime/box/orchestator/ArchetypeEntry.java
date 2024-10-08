package io.intino.ime.box.orchestator;

import java.util.List;

public class ArchetypeEntry {
	private final String path;
	private final List<String> builders;

	public ArchetypeEntry(String path, List<String> builders) {
		this.path = path;
		this.builders = builders;
	}

	public String path() {
		return path;
	}

	public List<String> builders() {
		return builders;
	}
}

