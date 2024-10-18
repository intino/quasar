package io.intino.ime.box.orchestator;

import java.io.BufferedWriter;
import java.util.*;

public class QuassarWriter {

	private final Map<String, String> variables;
	private final List<ArchetypeEntry> archetypeEntries;

	public QuassarWriter(Map<String, String> variables, List<ArchetypeEntry> archetypeEntries){
		this.variables = variables;
		this.archetypeEntries = archetypeEntries;
	}

	public String serialize() {
		StringBuilder builder = new StringBuilder();
		variables.forEach((k, v) -> builder.append("#").append(k).append("=").append(v).append("\n"));
		archetypeEntries.forEach(e -> builder.append("\n").append(e.toString()));
		return builder.toString();
	}
}
