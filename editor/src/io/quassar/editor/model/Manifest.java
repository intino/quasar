package io.quassar.editor.model;

import java.io.Serializable;

public class Manifest implements Serializable {
	private String id;
	private String name;
	private String version;
	private String commit;
	private String dsl;
	private String owner;

	public String id() {
		return id;
	}

	public Manifest id(String id) {
		this.id = id;
		return this;
	}

	public String name() {
		return name;
	}

	public Manifest name(String name) {
		this.name = name;
		return this;
	}

	public String version() {
		return version;
	}

	public Manifest version(String version) {
		this.version = version;
		return this;
	}

	public String commit() {
		return commit;
	}

	public Manifest commit(String commit) {
		this.commit = commit;
		return this;
	}

	public String dsl() {
		return dsl;
	}

	public Manifest dsl(String dsl) {
		this.dsl = dsl;
		return this;
	}

	public String owner() {
		return owner;
	}

	public Manifest owner(String owner) {
		this.owner = owner;
		return this;
	}
}
