package io.intino.ime.model;

import java.net.URI;
import java.time.Instant;

public final class Workspace {
	private String name;
	private String title;
	private User owner;
	private Instant lastModifyDate;
	private URI documentRoot;
	private String language;
	private boolean isPrivate = true;
	private String token;

	public String name() {
		return name;
	}

	public Workspace name(String name) {
		this.name = name;
		return this;
	}

	public String title() {
		return title;
	}

	public Workspace title(String title) {
		this.title = title;
		return this;
	}

	public User owner() {
		return owner;
	}

	public Workspace owner(User owner) {
		this.owner = owner;
		return this;
	}

	public Instant lastModifyDate() {
		return lastModifyDate;
	}

	public Workspace lastModifyDate(Instant lastModifyDate) {
		this.lastModifyDate = lastModifyDate;
		return this;
	}

	public URI documentRoot() {
		return documentRoot;
	}

	public Workspace documentRoot(URI documentRoot) {
		this.documentRoot = documentRoot;
		return this;
	}

	public String language() {
		return language;
	}

	public Workspace language(String value) {
		this.language = value;
		return this;
	}

	public boolean isPublic() {
		return !isPrivate;
	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public Workspace isPrivate(boolean value) {
		this.isPrivate = value;
		return this;
	}

	public String token() {
		return token;
	}

	public Workspace token(String token) {
		this.token = token;
		return this;
	}

	public static Workspace clone(Workspace workspace) {
		Workspace result = new Workspace();
		result.name = workspace.name;
		result.title = workspace.title;
		result.owner = workspace.owner;
		result.lastModifyDate = Instant.now();
		result.language = workspace.language;
		result.isPrivate = true;
		result.token = null;
		return result;
	}
}
