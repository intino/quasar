package io.quassar.editor.model;

import java.util.Objects;

public record GavCoordinates(String groupId, String artifactId, String version) {

	public static GavCoordinates fromString(String content) {
		String[] split = content.split(":");
		return new GavCoordinates(split[0], split[1], split[2]);
	}

	public static GavCoordinates fromString(Language language, LanguageRelease release) {
		return new GavCoordinates(language.group(), language.name(), release.version());
	}

	public boolean isEmpty() {
		return artifactId == null || artifactId.isEmpty();
	}

	public String languageId() {
		return Language.key(groupId, artifactId);
	}

	public boolean matches(Language language) {
		return language.group().equals(groupId) && language.name().equals(artifactId);
	}

	@Override
	public String toString() {
		String result = groupId != null && !groupId.isEmpty() ? groupId : "";
		return result + (!result.isEmpty() ? ":": "") + artifactId + ":" + version;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		var that = (GavCoordinates) obj;
		return Objects.equals(this.groupId, that.groupId) &&
				Objects.equals(this.artifactId, that.artifactId) &&
				Objects.equals(this.version, that.version);
	}
}
