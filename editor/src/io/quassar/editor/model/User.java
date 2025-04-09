package io.quassar.editor.model;

import java.util.ArrayList;
import java.util.List;

public class User {
	private String id;
	private String name;
	private List<String> adminTeams = new ArrayList<>();
	private List<String> memberTeams = new ArrayList<>();

	public static final String Anonymous = "anonymous";

	public String id() {
		return id;
	}

	public User id(String id) {
		this.id = id;
		return this;
	}

	public String name() {
		return name;
	}

	public User name(String name) {
		this.name = name;
		return this;
	}

	public List<String> adminTeams() {
		return adminTeams;
	}

	public User adminTeams(List<String> adminTeams) {
		this.adminTeams = adminTeams;
		return this;
	}

	public List<String> memberTeams() {
		return memberTeams;
	}

	public User memberTeams(List<String> memberTeams) {
		this.memberTeams = memberTeams;
		return this;
	}

}
