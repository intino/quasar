package io.quassar.editor.model;

import io.quassar.editor.box.util.SubjectHelper;
import io.quassar.editor.box.util.VersionNumberComparator;
import systems.intino.datamarts.subjectstore.model.Subject;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

public class Model extends SubjectWrapper {

	public static final String DraftRelease = "draft";

	public Model(Subject subject) {
		super(subject);
	}

	public static String qualifiedTitleFor(String project, String module) {
		return project + (module != null && !module.isEmpty() ? ": " + module : "");
	}

	public String id() {
		return subject.name();
	}

	public String name() {
		return getOrEmpty("name");
	}

	public void name(String value) {
		set("name", value);
	}

	public String title() {
		return get("title");
	}

	public void title(String value) {
		set("title", value);
		set("qualified-title", "");
	}

	public boolean isTitleQualified() {
		return !qualifiedTitle().isEmpty();
	}

	public String qualifiedTitle() {
		return getOrEmpty("qualified-title");
	}

	public String project() {
		return getOrEmpty("project");
	}

	public String module() {
		return getOrEmpty("module");
	}

	public void qualifiedTitle(String project, String module) {
		set("qualified-title", qualifiedTitleFor(project, module));
		set("project", project);
		set("module", module);
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
		return new GavCoordinates(subject.get("dsl-group"), subject.get("dsl-name"), subject.get("dsl-version"));
	}

	public void language(GavCoordinates value) {
		set("dsl-group", value.groupId());
		set("dsl-name", value.artifactId());
		set("dsl-version", value.version());
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

	public List<ModelRelease> releases() {
		Stream<Subject> result = subject.children().collect().stream().filter(s -> s.is(SubjectHelper.ModelReleaseType));
		return result.map(this::releaseOf).toList();
	}

	public ModelRelease release(String version) {
		return releases().stream().filter(r -> r.version().equals(version)).findFirst().orElse(null);
	}

	public ModelRelease lastRelease() {
		List<ModelRelease> releases = releases();
		return !releases.isEmpty() ? releases.stream().sorted((o1, o2) -> VersionNumberComparator.getInstance().compare(o2.version(), o1.version())).toList().getFirst() : null;
	}

	public boolean isPublic() {
		return isExample() || !isPrivate();
	}

	public boolean isPrivate() {
		return visibility() == Visibility.Private;
	}

	public void isPrivate(boolean value) {
		visibility(value ? Visibility.Private : Visibility.Public);
	}

	public Visibility visibility() {
		return get("visibility") == null ? Visibility.Private : Visibility.valueOf(get("visibility"));
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

	private ModelRelease releaseOf(Subject subject) {
		return new ModelRelease(subject);
	}

}
