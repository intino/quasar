package io.quassar.editor.model;

import systems.intino.datamarts.subjectstore.model.Subject;

import java.util.List;

public class User extends SubjectWrapper {

	public static final String Anonymous = "anonymous";
	public static final String Quassar = "quassar";

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

	public static final int DefaultLicenseTime = -999999999;
	public int licenseTime() {
		String time = get("license-time");
		return time != null ? Integer.parseInt(time) : DefaultLicenseTime;
	}

	public void licenseTime(int value) {
		set("license-time", String.valueOf(value));
	}

}
