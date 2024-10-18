package io.intino.ime.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Language {
	private String name;
	private String description;
	private boolean isPrivate;
	private String parent;
	private List<Operation> operations;
	private int modelsCount;
	private String owner;
	private String builder;
	private Instant createDate;
	private List<String> tagList;

	public Language(String name) {
		this.name = name;
		isPrivate = true;
		operations = new ArrayList<>();
		createDate = Instant.now();
		tagList = new ArrayList<>();
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

	public String builder() {
		return builder;
	}

	public Language builder(String value) {
		this.builder = value;
		return this;
	}

	public Instant createDate() {
		return createDate;
	}

	public Language createDate(Instant createDate) {
		this.createDate = createDate;
		return this;
	}

	public List<String> tags() {
		return tagList;
	}

	public Language tags(List<String> tagList) {
		this.tagList = tagList;
		return this;
	}
}
