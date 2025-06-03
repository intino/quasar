package io.quassar.editor.model;

import systems.intino.datamarts.subjectstore.model.Subject;

public class LanguageExecution extends SubjectWrapper {

	public LanguageExecution(Subject subject) {
		super(subject);
	}

	public String name() {
		return getOrEmpty("name");
	}

	public void name(String value) {
		set("name", value);
	}

	public enum Type { None, Local, Remote }
	public Type type() {
		return Type.valueOf(get("type"));
	}

	public void type(Type value) {
		set("type", value.name());
	}

	public String content() {
		return content(type());
	}

	public void content(String content) {
		switch (type()) {
			case Local: { set("local", content.replace("\n", "###n")); break; }
			case Remote: { set("remote", content.replace("\n", "###n")); break; }
		}
	}

	public String content(Type type) {
		return switch (type) {
			case Local -> getOrEmpty("local").replace("###n", "\n");
			case Remote -> getOrEmpty("remote").replace("###n", "\n");
			default -> "";
		};
	}

	public String installationUrl() {
		return getOrEmpty("installation-url");
	}

	public void installationUrl(String content) {
		set("installation-url", content);
	}

}
