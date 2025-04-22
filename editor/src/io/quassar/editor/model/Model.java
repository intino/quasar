package io.quassar.editor.model;

import systems.intino.datamarts.subjectstore.model.Subject;

import java.time.Instant;
import java.util.List;

public class Model extends SubjectWrapper {

	public static final String DraftRelease = "draft";

	public Model(Subject subject) {
		super(subject);
	}

	public String id() {
		return subject.name();
	}

	public String name() {
		return get("name");
	}

	public void name(String value) {
		set("name", value);
	}

	public String title() {
		return get("title");
	}

	public void title(String value) {
		set("title", value);
	}

	public String description() {
		return get("description");
	}

	public void description(String value) {
		set("description", value);
	}

	public GavCoordinates language() {
		return language(subject);
	}

	public static GavCoordinates language(Subject subject) {
		return new GavCoordinates(subject.get("language-group"), subject.get("language-name"), subject.get("language-version"));
	}

	public void language(GavCoordinates value) {
		set("language-group", value.groupId());
		set("language-name", value.artifactId());
		set("language-version", value.version());
	}

	public String owner() {
		return owner(subject);
	}

	public static String owner(Subject subject) {
		return subject.get("owner");
	}

	public void owner(String owner) {
		set("owner", owner);
	}

	public Instant createDate() {
		return Instant.parse(get("create-date"));
	}

	public void createDate(Instant date) {
		set("create-date", date.toString());
	}

	public List<String> collaborators() {
		return getList("collaborator");
	}

	public void collaborators(List<String> values) {
		putList("collaborator", values);
	}

	public void add(String collaborator) {
		put("collaborator", collaborator);
	}

	public boolean isPublic() {
		return !isPrivate();
	}

	public boolean isPrivate() {
		return visibility() == Visibility.Private;
	}

	public void isPrivate(boolean value) {
		visibility(value ? Visibility.Private : Visibility.Public);
	}

	public enum Visibility { Private, Public }
	public Visibility visibility() {
		return Visibility.valueOf(get("visibility"));
	}

	public void visibility(Visibility value) {
		set("visibility", value.name());
	}

	public boolean isExample() {
		return usage() == Usage.Example;
	}

	public boolean isTemplate() {
		return usage() == Usage.Template;
	}

	public enum Usage { Template, Example, EndUser }
	public Usage usage() {
		return Usage.valueOf(get("usage"));
	}

	public void usage(Usage usage) {
		set("usage", usage.name());
	}

	public boolean isDraft(String release) {
		return release != null && release.equals(Model.DraftRelease);
	}

}
