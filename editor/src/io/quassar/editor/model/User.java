package io.quassar.editor.model;

import java.util.ArrayList;
import java.util.List;

public class User {
	private String name;
	private List<Project> projectList = new ArrayList<>();

	public static final String Anonymous = "anonymous";

	public String name() {
		return name;
	}

	public User name(String name) {
		this.name = name;
		return this;
	}

	public List<Project> projectList() {
		return projectList;
	}

	public User projectList(List<Project> projectList) {
		this.projectList = projectList;
		return this;
	}
}
