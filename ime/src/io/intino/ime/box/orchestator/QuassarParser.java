package io.intino.ime.box.orchestator;

import java.util.*;

public class QuassarParser {

	// TODO CHECKER: lang project tara

	private final Map<String, String> variables = new HashMap<>();
	private final List<ArchetypeEntry> archetypeEntries = new ArrayList<>();

	public QuassarParser(String input) {
		parseInput(input);
	}

	private void parseInput(String input) {
		String[] lines = input.split("\n");
		Deque<String> pathStack = new ArrayDeque<>();

		for (String line : lines) {
			if (line.trim().isEmpty()) continue;
			if (line.startsWith("#")) processVariable(line);
			else processPath(line, pathStack);
		}
	}

	private void processPath(String line, Deque<String> pathStack) {
		int level = getIndentationLevel(line);
		while (pathStack.size() > level) pathStack.pop();
		if (line.contains("=>")) addPathWithBuilder(line, pathStack);
		else pathStack.push(line.trim());
	}

	private void addPathWithBuilder(String line, Deque<String> pathStack) {
		String[] parts = line.split("=>", 2);
		String key = parts[0].trim();
		String value = parts[1].trim();
		pathStack.push(key);
		String fullPath = String.join("/", reverseStack(pathStack));
		archetypeEntries.add(new ArchetypeEntry(fullPath, Arrays.stream(value.split(",")).map(String::trim).toList()));
	}

	private void processVariable(String line) {
		String[] parts = line.substring(1).split("=", 2);
		if (parts.length == 2) variables.put(parts[0].trim(), parts[1].trim());
	}

	private int getIndentationLevel(String line) {
		int count = 0;
		for (char c : line.toCharArray()) {
			if (c == '\t' || c == ' ') {
				count++;
			} else {
				break;
			}
		}
		return count;
	}

	private List<String> reverseStack(Deque<String> stack) {
		List<String> result = new ArrayList<>(stack);
		Collections.reverse(result);
		return result;
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

	public String pathOf(String builder, String prefix) {
		return archetypeEntries.stream()
				.filter(e -> e.builders().contains(builder))
				.map(ArchetypeEntry::path)
				.filter(path -> path.contains(prefix))
				.findFirst().get();
	}

	public String langQn() {
		return variables.get("lang").split(":")[0];
	}
}
