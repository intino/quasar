package io.quassar.editor.model;

import systems.intino.datamarts.subjectstore.model.Subject;

public class ModelRelease extends SubjectWrapper {

	public ModelRelease(Subject subject) {
		super(subject);
	}

	public String commit() {
		return get("commit");
	}

	public void commit(String value) {
		set("commit", value);
	}

	public String version() {
		return get("version");
	}

	public void version(String version) {
		set("version", version);
	}

	public GavCoordinates language() {
		return new GavCoordinates(get("language-group"), get("language-name"), get("language-version"));
	}

	public void language(GavCoordinates value) {
		set("language-group", value.groupId());
		set("language-name", value.artifactId());
		set("language-version", value.version());
	}

	public String owner() {
		return get("owner");
	}

	public void owner(String value) {
		set("owner", value);
	}

}
