package io.quassar.editor.model;

import systems.intino.datamarts.subjectstore.model.Subject;

public class LanguageExecution extends SubjectWrapper {

	public LanguageExecution(Subject subject) {
		super(subject);
	}

	public enum Type { None, Local, Remote }
	public Type type() {
		return Type.valueOf(get("type"));
	}

	public void type(Type value) {
		set("type", value.name());
	}

	public String remoteConfiguration() {
		return getOrEmpty("remote").replace("###n", "\n");
	}

	public void remoteConfiguration(String content) {
		set("remote", content.replace("\n", "###n"));
	}

	public enum LocalLanguage { Docker, Python, Maven, Custom }
	public String localConfiguration(LocalLanguage language) {
		return getOrEmpty("local-" + language.name()).replace("###n", "\n");
	}

	public void localConfiguration(LocalLanguage language, String content) {
		set("local-" + language.name(), content.replace("\n", "###n"));
	}

}
