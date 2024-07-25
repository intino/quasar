package io.intino.ime.box.workspaces;

import io.intino.tara.Tara;
import tara.dsl.Proteo;

import java.io.File;
import java.net.URI;
import java.time.Instant;

public class Workspace {
	public String name;
	public String title;
	public User owner;
	public Instant lastModifyDate;

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

	public Tara dsl() {
		return new Proteo(); //TODO
	}

	public URI uri() {
		return new File("root").toURI();//TODO
	}

	public static class User {
		private String name;
		private String fullName;

		public String name() {
			return name;
		}

		public User name(String name) {
			this.name = name;
			return this;
		}

		public String fullName() {
			return fullName;
		}

		public User fullName(String fullName) {
			this.fullName = fullName;
			return this;
		}
	}

}
