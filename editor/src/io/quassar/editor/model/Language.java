package io.quassar.editor.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Language {
	private String name;
	private Level level;
	private String parent;
	private String description;
	private String owner;
	private List<String> tagList;
	private Instant createDate;
	private transient ModelsProvider modelsProvider;

	public enum Level { L1, L2, L3 }

	public Language() {
		this.tagList = new ArrayList<>();
	}

	public Language(String name) {
		this.name = name;
		this.tagList = new ArrayList<>();
		this.createDate = Instant.now();
	}

	public void modelsProvider(ModelsProvider provider) {
		this.modelsProvider = provider;
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

	public Level level() {
		return level;
	}

	public Language level(Level level) {
		this.level = level;
		return this;
	}

	public String description() {
		return description;
	}

	public Language description(String description) {
		this.description = description;
		return this;
	}

	public String owner() {
		return owner;
	}

	public Language owner(String owner) {
		this.owner = owner;
		return this;
	}

	public String parent() {
		return parent;
	}

	public Language parent(String parent) {
		this.parent = parent;
		return this;
	}

	public List<String> models() {
		return modelsProvider.models();
	}

	public long modelsCount() {
		return modelsProvider.models().size();
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

	public boolean isFoundational() {
		return parent == null || parent.isEmpty();
	}

	public interface ModelsProvider {
		List<String> models();
	}

}
