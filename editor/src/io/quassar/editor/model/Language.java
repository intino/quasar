package io.quassar.editor.model;

import io.quassar.editor.box.util.VersionNumberComparator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Language {
	private String name;
	private String group;
	private String metamodel;
	private Level level;
	private GavCoordinates parent;
	private String title;
	private String description;
	private List<String> accessList;
	private String acknowledgment;
	private String credits;
	private String citation;
	private String license;
	private List<String> tagList;
	private List<LanguageRelease> releaseList;
	private Instant createDate;

	public static final String FileExtension = ".tara";
	public static final String QuassarGroup = "tara.dsl";
	public static final String Metta = "meta";

	public static String groupFrom(String id) {
		return id.split("\\.")[0];
	}

	public static String nameFrom(String id) {
		return id.split("\\.")[1];
	}

	public static String id(String group, String name) {
		if (group.equalsIgnoreCase(Language.QuassarGroup)) return name;
		return name + "." + group;
	}

	public String id() {
		return id(group, name);
	}

	public enum Level { L1, L2, L3 }

	public Language() {
		this.tagList = new ArrayList<>();
		this.accessList = new ArrayList<>();
		this.releaseList = new ArrayList<>();
	}

	public Language(String name) {
		this.name = name;
		this.tagList = new ArrayList<>();
		this.createDate = Instant.now();
		this.releaseList = new ArrayList<>();
	}

	public String group() {
		return group;
	}

	public Language group(String group) {
		this.group = group;
		return this;
	}

	public boolean isQuassarLanguage() {
		return group.equals(Language.QuassarGroup);
	}

	public String name() {
		return name;
	}

	public Language name(String name) {
		this.name = name;
		return this;
	}

	public String metamodel() {
		return metamodel;
	}

	public Language metamodel(String metamodel) {
		this.metamodel = metamodel;
		return this;
	}

	public Level level() {
		return level;
	}

	public Language level(Level level) {
		this.level = level;
		return this;
	}

	public GavCoordinates parent() {
		return parent;
	}

	public Language parent(GavCoordinates parent) {
		this.parent = parent;
		return this;
	}

	public String title() {
		return title;
	}

	public Language title(String value) {
		this.title = value;
		return this;
	}

	public String description() {
		return description;
	}

	public Language description(String description) {
		this.description = description;
		return this;
	}

	public List<String> access() {
		return accessList;
	}

	public Language access(List<String> values) {
		this.accessList = values;
		return this;
	}

	public String acknowledgment() {
		return acknowledgment;
	}

	public Language acknowledgment(String acknowledgment) {
		this.acknowledgment = acknowledgment;
		return this;
	}

	public String credits() {
		return credits;
	}

	public Language credits(String credits) {
		this.credits = credits;
		return this;
	}

	public String citation() {
		return citation;
	}

	public Language citation(String citation) {
		this.citation = citation;
		return this;
	}

	public String license() {
		return license;
	}

	public Language license(String license) {
		this.license = license;
		return this;
	}

	public List<String> tags() {
		return tagList;
	}

	public Language tags(List<String> tagList) {
		this.tagList = tagList;
		return this;
	}

	public List<LanguageRelease> releases() {
		return releaseList;
	}

	public LanguageRelease release(String version) {
		return releaseList.stream().filter(r -> r.version().equals(version)).findFirst().orElse(null);
	}

	public LanguageRelease lastRelease() {
		List<LanguageRelease> releases = releases();
		return !releases.isEmpty() ? releases.stream().sorted((o1, o2) -> VersionNumberComparator.getInstance().compare(o2.version(), o1.version())).toList().getFirst() : null;
	}

	public Language releases(List<LanguageRelease> releaseList) {
		this.releaseList = releaseList;
		return this;
	}

	public void add(LanguageRelease release) {
		this.releaseList.add(release);
	}

	public Instant createDate() {
		return createDate;
	}

	public Language createDate(Instant createDate) {
		this.createDate = createDate;
		return this;
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
}
