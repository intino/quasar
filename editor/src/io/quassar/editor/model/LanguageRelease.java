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

	public String commit() {
		return get("commit");
	}

	public void commit(String commit) {
		set("commit", commit);
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

	public void removeExample(String example) {
		del("example", example);
	}

	public LanguageExecution execution() {
		Stream<Subject> result = subject.children().collect().stream().filter(s -> s.is(SubjectHelper.LanguageExecutionType));
		return result.findFirst().map(this::executionOf).orElse(null);
	}

	private LanguageExecution executionOf(Subject subject) {
		return new LanguageExecution(subject);
	}

}
