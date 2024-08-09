package io.intino.ime.model;

import java.time.Instant;

public class Language {
	private String name;
	private String version;
	private LanguageInfo info;
	private String owner;
	private boolean isPrivate = true;
	private Instant createDate;

	public Language(String id) {
		this.name = Language.nameOf(id);
		this.version = Language.versionOf(id);
	}

	public static String id(String name, String version) {
		return name + ":" + version;
	}

	public static String nameOf(String id) {
		return id.split(":")[0];
	}

	public static String versionOf(String id) {
		return id.split(":")[1];
	}

	public String id() {
		return name + ":" + version;
	}

	public String name() {
		return name;
	}

	public Language name(String name) {
		this.name = name;
		return this;
	}

	public String version() {
		return version;
	}

	public Language version(String version) {
		this.version = version;
		return this;
	}

	public LanguageInfo info() {
		return info;
	}

	public LanguageInfo.Level level() {
		return info.level();
	}

	public Language info(LanguageInfo value) {
		this.info = value;
		return this;
	}

	public String owner() {
		return owner;
	}

	public Language owner(String value) {
		this.owner = value;
		return this;
	}

	public boolean isPublic() {
		return !isPrivate;
	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public Language isPrivate(boolean value) {
		this.isPrivate = value;
		return this;
	}

	public Instant createDate() {
		return createDate;
	}

	public Language createDate(Instant createDate) {
		this.createDate = createDate;
		return this;
	}
}
