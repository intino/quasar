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
	public static final String FoundationalGroup = "tara.dsl";
	public static final String QuassarGroup = "io.quassar";
	public static final String Metta = "metta";

	public static String key(String group, String name) {
		if (group == null || group.isEmpty() || group.equalsIgnoreCase(Language.QuassarGroup) || group.equalsIgnoreCase(Language.FoundationalGroup)) return name;
		return group + "." + name;
	}

	public static String groupFrom(String id) {
		return id.contains(".") ? id.substring(0, id.indexOf(".")) : "";
	}

	public static String nameFrom(String id) {
		return id.contains(".") ? id.substring(id.lastIndexOf(".")+1) : id;
	}

	public String id() {
		return subject.name();
	}

	public String key() {
		return key(group(), name());
	}

	public enum Level { L1, L2, L3 }

	public Language(Subject subject) {
		super(subject);
	}

	public String group() {
		String group = getOrEmpty("group");
		return !group.isEmpty() ? group : Language.QuassarGroup;
	}

	public void group(String group) {
		set("group", group);
	}

	public boolean isFoundational() {
		String group = group();
		return group != null && group.equals(Language.FoundationalGroup);
	}

	public String name() {
		return getOrEmpty("name");
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
		return new GavCoordinates(getOrEmpty("parent-group"), getOrEmpty("parent-name"), getOrEmpty("parent-version"));
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
		return getOrEmpty("description");
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

	public String citation() {
		return getOrEmpty("citation");
	}

	public void citation(String citation) {
		set("citation", citation);
	}

	public String citationLink() {
		return getOrEmpty("citation-link");
	}

	public void citationLink(String value) {
		set("citation-link", value);
	}

	public String license() {
		return getOrEmpty("license");
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
		return Objects.equals(key(), language.key());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(key());
	}

	private LanguageRelease releaseOf(Subject subject) {
		return new LanguageRelease(subject);
	}

}
