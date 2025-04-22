package io.quassar.editor.model;

import systems.intino.datamarts.subjectstore.model.Subject;

import java.util.List;

public class User extends SubjectWrapper {

	public static final String Anonymous = "anonymous";

	public User(Subject subject) {
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

	public void name(String name) {
		set("name", name);
	}

	public List<String> adminTeams() {
		return getList("admin-team");
	}

	public void adminTeams(List<String> values) {
		putList("admin-team", values);
	}

	public List<String> memberTeams() {
		return getList("member-team");
	}

	public void memberTeams(List<String> values) {
		putList("member-team", values);
	}

}
