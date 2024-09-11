package io.intino.ime.model;

import java.net.URI;
import java.time.Instant;

public final class Model {
	private String name;
	private String title;
	private User owner;
	private Instant lastModifyDate;
	private URI workspaceDirectory;
	private String language;
	private boolean isPrivate = true;
	private String token;

	public String name() {
		return name;
	}

	public Model name(String name) {
		this.name = name;
		return this;
	}

	public String title() {
		return title;
	}

	public Model title(String title) {
		this.title = title;
		return this;
	}

	public User owner() {
		return owner;
	}

	public Model owner(User owner) {
		this.owner = owner;
		return this;
	}

	public Instant lastModifyDate() {
		return lastModifyDate;
	}

	public Model lastModifyDate(Instant lastModifyDate) {
		this.lastModifyDate = lastModifyDate;
		return this;
	}

	public URI workspaceRootDirectory() {
		return workspaceDirectory;
	}

	public Model workspaceRootDirectory(URI root) {
		this.workspaceDirectory = root;
		return this;
	}

	public String language() {
		return language;
	}

	public Model language(String value) {
		this.language = value;
		return this;
	}

	public boolean isTemporal() {
		return isPublic() && (owner == null || owner.name().equals("anonymous"));
	}

	public boolean isPublic() {
		return !isPrivate;
	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public Model isPrivate(boolean value) {
		this.isPrivate = value;
		return this;
	}

	public String token() {
		return token;
	}

	public Model token(String token) {
		this.token = token;
		return this;
	}

	public static Model clone(Model model) {
		Model result = new Model();
		result.name = model.name;
		result.title = model.title;
		result.owner = model.owner;
		result.lastModifyDate = Instant.now();
		result.language = model.language;
		result.isPrivate = true;
		result.token = null;
		return result;
	}

}
