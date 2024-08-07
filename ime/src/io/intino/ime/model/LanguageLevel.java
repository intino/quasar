package io.intino.ime.model;

public class LanguageLevel {
	private Level value;
	private String metaLanguage;

	public enum Level { L1, L2, L3 }

	public static LanguageLevel from(String id) {
		String[] split = id.split("\\(");
		return new LanguageLevel().value(Level.valueOf(split[0])).metaLanguage(split.length > 1 ? split[1].substring(0, split[1].length()-1) : null);
	}

	public Level value() {
		return value;
	}

	public LanguageLevel value(Level level) {
		this.value = level;
		return this;
	}

	public String metaLanguage() {
		return metaLanguage;
	}

	public LanguageLevel metaLanguage(String metaLanguage) {
		this.metaLanguage = metaLanguage;
		return this;
	}

}

