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
		return new GavCoordinates(get("dsl-collection"), get("dsl-name"), get("dsl-version"));
	}

	public void language(GavCoordinates value) {
		set("dsl-collection", value.groupId());
		set("dsl-name", value.artifactId());
		set("dsl-version", value.version());
	}

	public String owner() {
		return get("owner");
	}

	public void owner(String value) {
		set("owner", value);
	}

}
