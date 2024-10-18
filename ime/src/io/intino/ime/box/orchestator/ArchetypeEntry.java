package io.intino.ime.box.orchestator;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArchetypeEntry {
	public static final Pattern PATTERN = Pattern.compile("\\[(.*?)]");
	private final String path;
	private final List<String> builders;

	public ArchetypeEntry(String path, List<String> builders) {
		this.path = path;
		this.builders = builders;
	}

	public ArchetypeEntry(String path, String scaffold, List<String> builders) {
		this.path = path + (scaffold != null ? "[" + scaffold + "]" : "");
		this.builders = builders;
	}

	public String path() {
		return path.contains("[") ? path.substring(0, path.indexOf("[")) : path;
	}

	public String scaffold() {
		Matcher matcher = PATTERN.matcher(path);
		return matcher.find() ? matcher.group(1) : null;
	}

	public List<String> builders() {
		return builders;
	}

	@Override
	public String toString() {
		return path + " <= " + String.join(", ", builders);
	}
}

