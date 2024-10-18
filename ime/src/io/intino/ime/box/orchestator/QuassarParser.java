package io.intino.ime.box.orchestator;

import java.util.*;

public class QuassarParser {

	private final Map<String, String> variables = new HashMap<>();
	private final List<ArchetypeEntry> archetypeEntries = new ArrayList<>();

	public QuassarParser(String input) {
		parseInput(input);
	}

	private void parseInput(String input) {
		String[] lines = input.split("\n");

		for (String line : lines) {
			if (line.trim().isEmpty()) continue;
			if (line.startsWith("#")) processVariable(line);
			else processPath(line);
		}
	}

	private void processPath(String line) {
		String[] split = line.split("<=");
		archetypeEntries.add(new ArchetypeEntry(split[0].trim(), Arrays.stream(split[1].split(",")).map(String::trim).toList()));
	}

	private void processVariable(String line) {
		String[] parts = line.substring(1).split("=", 2);
		if (parts.length == 2) variables.put(parts[0].trim(), parts[1].trim());
	}

	public String valueOf(String varName) {
		return variables.get(varName);
	}

	public Map<String, String> variables() {
		return variables;
	}

	public List<ArchetypeEntry> paths() {
		return archetypeEntries;
	}

	public String langName() {
		return last(langQn().split("\\."));
	}

	private String last(String[] split) {
		return split[split.length - 1];
	}

	public String langVersion() {
		return variables.get("lang").split(":")[1];
	}

	public String projectName() {
		return variables.get("project").split(":")[0];
	}

	public String projectVersion() {
		return variables.get("project").split(":")[1];
	}

	public String tara() {
		return archetypeEntries.stream()
				.flatMap(e -> e.builders().stream())
				.filter(b -> b.contains("io.intino.tara"))
				.findFirst().get();
	}

	public String pathOf(String builder) {
		return archetypeEntries.stream()
				.filter(e -> e.builders().contains(builder))
				.map(ArchetypeEntry::path)
				.findFirst().get();
	}

	public String langQn() {
		return variables.get("lang").split(":")[0];
	}

	public String codePackage() {
		return variables.get("package");
	}
}
