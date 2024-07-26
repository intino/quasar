package io.intino.ime.box.workspaces;

import java.net.URI;
import java.time.Instant;

public final class Workspace {
	private String name;
	private String title;
	private User owner;
	private Instant lastModifyDate;
	private URI documentRoot;
	private String dsl;
	private long executionsCount;

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

	public String dsl() {
		return dsl;
	}

	public Workspace dsl(String dsl) {
		this.dsl = dsl;
		return this;
	}

	public long executionsCount() {
		return executionsCount;
	}

	public Workspace executionsCount(long executionsCount) {
		this.executionsCount = executionsCount;
		return this;
	}

	public record User(String name, String fullName) {
	}

}
