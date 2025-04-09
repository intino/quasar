package io.quassar.editor.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Model {
	private String id;
	private String name;
	private String title;
	private String description;
	private GavCoordinates language;
	private String owner;
	private Instant createDate;
	private List<String> collaborators = new ArrayList<>();
	private boolean isPrivate = false;

	public static final String DraftRelease = "draft";
	public static final String Template = "__template";

	public String id() {
		return id;
	}

	public Model id(String id) {
		this.id = id;
		return this;
	}

	public String name() {
		return name;
	}

	public Model name(String name) {
		this.name = name;
		return this;
	}

	public String title() {
		return title;
	}

	public Model title(String title) {
		this.title = title;
		return this;
	}

	public String description() {
		return description;
	}

	public Model description(String description) {
		this.description = description;
		return this;
	}

	public GavCoordinates language() {
		return language;
	}

	public Model language(GavCoordinates value) {
		this.language = value;
		return this;
	}

	public String owner() {
		return owner;
	}

	public Model owner(String owner) {
		this.owner = owner;
		return this;
	}

	public Instant createDate() {
		return createDate;
	}

	public Model createDate(Instant date) {
		this.createDate = date;
		return this;
	}

	public List<String> collaborators() {
		return collaborators;
	}

	public void collaborators(List<String> value) {
		this.collaborators = new ArrayList<>(value);
	}

	public void add(String collaborator) {
		this.collaborators.add(collaborator);
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

	public static Model clone(Model model) {
		Model result = new Model();
		result.language = model.language();
		result.owner = model.owner();
		result.isPrivate = true;
		return result;
	}

	public boolean isDraft(String release) {
		return release != null && release.equals(Model.DraftRelease);
	}

	public boolean isTemplate() {
		return name().equals(Model.Template);
	}

}
