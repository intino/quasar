package io.quassar.editor.model;

import systems.intino.datamarts.subjectindex.model.Subject;

import java.util.List;

public class LanguageRelease extends SubjectWrapper {

	public LanguageRelease(Subject subject) {
		super(subject);
	}

	public String version() {
		return get("version");
	}

	public void version(String version) {
		set("version", version);
	}

	public String template() {
		return get("template");
	}

	public void template(String template) {
		set("template", template);
	}

	public List<String> examples() {
		return getList("example");
	}

	public void examples(List<String> examples) {
		putList("example", examples);
	}

	public void addExample(String example) {
		put("example", example);
	}
}
