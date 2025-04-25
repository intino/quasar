package io.quassar.editor.model;

import io.quassar.editor.box.util.SubjectHelper;
import io.quassar.editor.box.util.VersionNumberComparator;
import systems.intino.datamarts.subjectstore.model.Subject;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class Language extends SubjectWrapper {

	public static final String FileExtension = ".tara";
	public static final String QuassarGroup = "tara.dsl";
	public static final String Metta = "meta";

	public static String id(String group, String name) {
		if (group == null || group.equalsIgnoreCase(Language.QuassarGroup)) return name;
		return group + "." + name;
	}

	public String id() {
		return id(group(), name());
	}

	public enum Level { L1, L2, L3 }

	public Language(Subject subject) {
		super(subject);
	}

	public String group() {
		return get("group");
	}

	public void group(String group) {
		set("group", group);
	}

	public boolean isQuassarLanguage() {
		return group().equals(Language.QuassarGroup);
	}

	public String name() {
		return get("name");
	}

	public void name(String value) {
		set("name", value);
	}

	public String metamodel() {
		return get("metamodel");
	}

	public void metamodel(String value) {
		set("metamodel", value);
	}

	public Level level() {
		return Level.valueOf(get("level"));
	}

	public void level(Level value) {
		set("level", value.name());
	}

	public GavCoordinates parent() {
		return new GavCoordinates(get("parent-group"), get("parent-name"), get("parent-version"));
	}

	public void parent(GavCoordinates value) {
		set("parent-group", value.groupId());
		set("parent-name", value.artifactId());
		set("parent-version", value.version());
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

	public List<String> grantAccessList() {
		return getList("access");
	}

	public void grantAccessList(List<String> values) {
		putList("access", values);
	}

	public String acknowledgment() {
		return get("acknowledgment");
	}

	public void acknowledgment(String acknowledgment) {
		set("acknowledgment", acknowledgment);
	}

	public String credits() {
		return get("credits");
	}

	public void credits(String credits) {
		set("credits", credits);
	}

	public String citation() {
		return get("citation");
	}

	public void citation(String citation) {
		set("citation", citation);
	}

	public String license() {
		return get("license");
	}

	public void license(String license) {
		set("license", license);
	}

	public List<String> tags() {
		return getList("tag");
	}

	public void tags(List<String> tagList) {
		putList("tag", tagList);
	}

	public List<LanguageRelease> releases() {
		Stream<Subject> result = subject.children().collect().stream().filter(s -> s.is(SubjectHelper.LanguageReleaseType));
		return result.map(this::releaseOf).toList();
	}

	public LanguageRelease release(String version) {
		return releases().stream().filter(r -> r.version().equals(version)).findFirst().orElse(null);
	}

	public LanguageRelease lastRelease() {
		List<LanguageRelease> releases = releases();
		return !releases.isEmpty() ? releases.stream().sorted((o1, o2) -> VersionNumberComparator.getInstance().compare(o2.version(), o1.version())).toList().getFirst() : null;
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

	public Visibility visibility() {
		return get("visibility") == null ? Visibility.Private : Visibility.valueOf(get("visibility"));
	}

	public void visibility(Visibility value) {
		set("visibility", value.name());
	}

	public Instant createDate() {
		return Instant.parse(get("create-date"));
	}

	public void createDate(Instant createDate) {
		set("create-date", createDate.toString());
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		Language language = (Language) o;
		return Objects.equals(id(), language.id());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id());
	}

	private LanguageRelease releaseOf(Subject subject) {
		return new LanguageRelease(subject);
	}

}
