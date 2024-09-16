package io.intino.ime.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Model {
	private String name;
	private Instant lastModifyDate;
	private String title;
	private User owner;
	private transient String version;
	private String language;
	private boolean isPrivate = true;
	private String token;
	private Map<String, Version> versionMap = new HashMap<>();

	public static String id(String name, String version) {
		return name + ":" + version;
	}

	public String id() {
		return id(name, version);
	}

	public static String nameOf(String id) {
		return id.split(":")[0];
	}

	public static String versionOf(String id) {
		String[] split = id.split(":");
		return split.length > 1 ? split[1] : "1.0.0";
	}

	public String name() {
		return name;
	}

	public Model name(String name) {
		this.name = name;
		return this;
	}

	public String version() {
		return version != null ? version : "1.0.0";
	}

	public Model version(String version) {
		this.version = version;
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

	public List<Version> versions() {
		return new ArrayList<>(versionMap.values());
	}

	public void add(Version version) {
		versionMap.put(version.id(), version);
	}

	public Map<String, Version> versionMap() {
		return versionMap;
	}

	public Model versionMap(Map<String, Version> versionMap) {
		this.versionMap = versionMap;
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

	public static class Version {
		private String id;
		private String metamodelVersion;
		private String builderUrl;

		public Version(String id, String metamodelVersion, String builderUrl) {
			this.id = id;
			this.metamodelVersion = metamodelVersion;
			this.builderUrl = builderUrl;
		}

		public String id() {
			return id;
		}

		public Version id(String id) {
			this.id = id;
			return this;
		}

		public String metamodelVersion() {
			return metamodelVersion;
		}

		public Version metamodelVersion(String metamodelVersion) {
			this.metamodelVersion = metamodelVersion;
			return this;
		}

		public String builderUrl() {
			return builderUrl;
		}

		public Version builderUrl(String builderUrl) {
			this.builderUrl = builderUrl;
			return this;
		}
	}

}
