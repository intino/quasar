package io.quassar.editor.model;

import io.quassar.editor.box.util.SubjectHelper;
import systems.intino.datamarts.subjectstore.model.Subject;

import java.util.List;

public class LanguageTool extends SubjectWrapper {

	public LanguageTool(Subject subject) {
		super(subject);
	}

	public String name() {
		return subject.name();
	}

	public enum Type { Docker, Site, Manual }
	public Type type() {
		return Type.valueOf(get("type"));
	}

	public void type(Type value) {
		set("type", value.name());
	}

	public List<Parameter> parameters() {
		List<Subject> result = subject.children().type(SubjectHelper.LanguageToolParameter).collect();
		return result.stream().map(this::parameterOf).toList();
	}

	public Parameter parameter(String name) {
		return parameters().stream().filter(r -> r.name().equals(name)).findFirst().orElse(null);
	}

	private Parameter parameterOf(Subject subject) {
		return new Parameter(subject);
	}

	public static class Parameter extends SubjectWrapper {

		public Parameter(Subject subject) {
			super(subject);
		}

		public String name() {
			return subject.name();
		}

		public String value() {
			return get("value");
		}

		public void value(String value) {
			set("value", value);
		}

	}
}
