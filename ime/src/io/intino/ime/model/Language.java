package io.intino.ime.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Language {
	private String name;
	private String description;
	private boolean isPrivate = true;
	private String parent;
	private List<Operation> operations = new ArrayList<>();
	private int modelsCount;
	private String owner;
	private String dockerImageUrl;
	private Instant createDate;
	private LanguageLevel level;

	public Language(String name) {
		this.name = name;
		createDate = Instant.now();
	}

	public static String nameOf(String id) {
		return id.split(":")[0];
	}

	public static String versionOf(String id) {
		String[] split = id.split(":");
		return split.length > 1 ? split[1] : null;
	}

	public String name() {
		return name;
	}

	public Language name(String name) {
		this.name = name;
		return this;
	}

	public String description() {
		return description;
	}

	public Language description(String description) {
		this.description = description;
		return this;
	}

	public boolean isPublic() {
		return isFoundational() || !isPrivate;
	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public boolean isFoundational() {
		return parent == null || parent.isEmpty();
	}

	public Language isPrivate(boolean value) {
		this.isPrivate = value;
		return this;
	}

	public String parent() {
		return parent;
	}

	public Language parent(String parent) {
		this.parent = parent;
		return this;
	}

	public List<Operation> operations() {
		return operations;
	}

	public Operation operation(String name) {
		return operations.stream().filter(o -> o.name().equals(name)).findFirst().orElse(null);
	}

	public Language operations(List<Operation> operations) {
		this.operations = operations;
		return this;
	}

	public Language add(Operation operation) {
		this.operations.add(operation);
		return this;
	}

	public int modelsCount() {
		return modelsCount;
	}

	public Language modelsCount(int modelsCount) {
		this.modelsCount = modelsCount;
		return this;
	}

	public String owner() {
		return owner;
	}

	public Language owner(String value) {
		this.owner = value;
		return this;
	}

	public String dockerImageUrl() {
		return dockerImageUrl;
	}

	public Language dockerImageUrl(String value) {
		this.dockerImageUrl = value;
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
