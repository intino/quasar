package io.quassar.editor.model;

import systems.intino.datamarts.subjectstore.model.Subject;

import java.time.Instant;
import java.util.List;

public class Collection extends SubjectWrapper {

	public Collection(Subject subject) {
		super(subject);
	}

	public String id() {
		return subject.name();
	}

	public String name() {
		return getOrEmpty("name");
	}

	public void name(String value) {
		set("name", value);
	}

	public String owner() {
		return owner(subject);
	}

	public static String owner(Subject subject) {
		return subject.get("owner");
	}

	public void owner(String owner) {
		set("owner", owner);
	}

	public Instant createDate() {
		String value = get("create-date");
		return value != null ? Instant.parse(value) : null;
	}

	public void createDate(Instant date) {
		set("create-date", date.toString());
	}

	public Instant updateDate() {
		String value = get("update-date");
		return value != null ? Instant.parse(value) : null;
	}

	public void updateDate(Instant date) {
		set("update-date", date.toString());
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

}
