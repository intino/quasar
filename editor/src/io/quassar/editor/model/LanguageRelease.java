package io.quassar.editor.model;

import io.quassar.editor.box.util.SubjectHelper;
import systems.intino.datamarts.subjectstore.model.Subject;

import java.util.List;
import java.util.stream.Stream;

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

	public List<LanguageTool> tools() {
		List<Subject> result = subject.children().type(SubjectHelper.LanguageReleaseTool).collect();
		return result.stream().map(this::toolOf).toList();
	}

	public LanguageTool tool(String name) {
		return tools().stream().filter(r -> r.name().equals(name)).findFirst().orElse(null);
	}

	private LanguageTool toolOf(Subject subject) {
		return new LanguageTool(subject);
	}
}
