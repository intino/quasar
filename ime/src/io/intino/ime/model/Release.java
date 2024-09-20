package io.intino.ime.model;

public class Release {
	private String language;
	private String model;
	private LanguageLevel level;
	private String version;

	public Release(String language, String model, LanguageLevel level, String version) {
		this.language = language;
		this.model = model;
		this.level = level;
		this.version = version;
	}

	public String id() {
		return language + ":" + version;
	}

	public String language() {
		return language;
	}

	public Release language(String value) {
		this.language = value;
		return this;
	}

	public String model() {
		return model;
	}

	public Release model(String model) {
		this.model = model;
		return this;
	}

	public LanguageLevel level() {
		return level;
	}

	public Release level(LanguageLevel level) {
		this.level = level;
		return this;
	}

	public String version() {
		return version;
	}

	public Release version(String version) {
		this.version = version;
		return this;
	}
}
