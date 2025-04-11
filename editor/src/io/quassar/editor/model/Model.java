package io.quassar.editor.model;

import systems.intino.datamarts.subjectindex.model.Subject;

import java.time.Instant;
import java.util.List;

public class Model extends SubjectWrapper {

	public static final String DraftRelease = "draft";

	public Model(Subject subject) {
		super(subject);
	}

	public String id() {
		return get("id");
	}

	public void id(String value) {
		set("id", value);
	}

	public String name() {
		return get("name");
	}

	public void name(String value) {
		set("name", value);
	}

	public String title() {
		return get("title");
	}

	public void title(String value) {
		set("title", value);
	}

	public String description() {
		return get("description");
	}

	public void description(String value) {
		set("description", value);
	}

	public GavCoordinates language() {
		return GavCoordinates.fromString(get("language"));
	}

	public void language(GavCoordinates value) {
		set("language", value.toString());
	}

	public String owner() {
		return get("owner");
	}

	public void owner(String owner) {
		set("owner", owner);
	}

	public Instant createDate() {
		return Instant.parse(get("create-date"));
	}

	public void createDate(Instant date) {
		set("create-date", date.toString());
	}

	public List<String> collaborators() {
		return getList("collaborator");
	}

	public void collaborators(List<String> values) {
		putList("collaborator", values);
	}

	public void add(String collaborator) {
		put("collaborator", collaborator);
	}

	public boolean isPublic() {
		return !isPrivate();
	}

	public boolean isPrivate() {
		return Boolean.parseBoolean(get("is-private"));
	}

	public void isPrivate(boolean value) {
		set("is-private", String.valueOf(value));
	}

	public boolean isTemplate() {
		return Boolean.parseBoolean(get("is-template"));
	}

	public void isTemplate(boolean value) {
		set("is-template", String.valueOf(value));
	}

	public boolean isDraft(String release) {
		return release != null && release.equals(Model.DraftRelease);
	}

}
