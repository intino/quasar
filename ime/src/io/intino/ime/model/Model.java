package io.intino.ime.model;

public final class Model {
	private String id;
	private String label;
	private String modelingLanguage;
	private String releasedLanguage;
	private String owner;
	private boolean isPrivate = true;

	public static final String DefaultOwner = "anonymous";

	public String id() {
		return id;
	}

	public Model id(String id) {
		this.id = id;
		return this;
	}

	public String label() {
		return label;
	}

	public Model label(String label) {
		this.label = label;
		return this;
	}

	public String modelingLanguage() {
		return modelingLanguage;
	}

	public Model modelingLanguage(String value) {
		this.modelingLanguage = value;
		return this;
	}

	public String owner() {
		return owner;
	}

	public Model owner(String owner) {
		this.owner = owner;
		return this;
	}

	public String releasedLanguage() {
		return releasedLanguage;
	}

	public Model releasedLanguage(String releasedLanguage) {
		this.releasedLanguage = releasedLanguage;
		return this;
	}

	public boolean isTemporal() {
		return isPublic() && (owner == null || owner.equalsIgnoreCase(Model.DefaultOwner));
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
		result.label = model.label();
		result.modelingLanguage = model.modelingLanguage();
		result.releasedLanguage = model.releasedLanguage();
		result.owner = model.owner();
		result.isPrivate = true;
		return result;
	}

}
