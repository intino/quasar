package io.quassar.editor.model;

import java.util.ArrayList;
import java.util.List;

public class LanguageRelease {
	private String version;
	private String template;
	private List<String> examples = new ArrayList<>();

	public String version() {
		return version;
	}

	public LanguageRelease version(String version) {
		this.version = version;
		return this;
	}

	public String template() {
		return template;
	}

	public LanguageRelease template(String template) {
		this.template = template;
		return this;
	}

	public List<String> examples() {
		return examples;
	}

	public LanguageRelease examples(List<String> examples) {
		this.examples = examples;
		return this;
	}
}
