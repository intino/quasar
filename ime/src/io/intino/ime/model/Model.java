package io.intino.ime.model;

public final class 	Model {
	private String id;
	private String label;
	private String modelingLanguage;
	private String releasedLanguage;
	private String owner;
	private GitSettings gitSettings = GitSettings.Empty();
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

	public GitSettings gitSettings() {
		return gitSettings;
	}

	public Model gitSettings(GitSettings gitSettings) {
		this.gitSettings = gitSettings;
		return this;
	}

	public record GitSettings(String url, String branch) {
		public static GitSettings Empty() {
			return new GitSettings(null, null);
		}

		public boolean equals(GitSettings settings) {
			return url != null && settings.url != null && url.equals(settings.url) &&
				   branch != null && settings.branch != null && branch.equals(settings.branch);
		}

		public boolean defined() {
			return url != null && branch != null;
		}
	}

	public static Model clone(Model model) {
		Model result = new Model();
		result.label = model.label();
		result.modelingLanguage = model.modelingLanguage();
		result.releasedLanguage = model.releasedLanguage();
		result.owner = model.owner();
		result.isPrivate = true;
		result.gitSettings = model.gitSettings();
		return result;
	}

}
