package io.quassar.editor.model;

import com.google.gson.annotations.SerializedName;
import systems.intino.datamarts.subjectindex.model.Subject;

public class ModelRelease extends SubjectWrapper {

	public ModelRelease(Subject subject) {
		super(subject);
	}

	@SerializedName("version")
	public String version() {
		return get("version");
	}

	public void version(String version) {
		set("version", version);
	}

	@SerializedName("language")
	public GavCoordinates language() {
		return GavCoordinates.fromString(get("language"));
	}

	public void language(GavCoordinates value) {
		set("language", value.toString());
	}

	@SerializedName("owner")
	public String owner() {
		return get("owner");
	}

	public void owner(String value) {
		set("owner", value);
	}

}
