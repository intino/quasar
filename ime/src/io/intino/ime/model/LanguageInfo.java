package io.intino.ime.model;

public class LanguageInfo {
	private Level level;
	private String metaLanguage;

	public enum Level { L1, L2, L3 }

	public static LanguageInfo from(String id) {
		String[] split = id.split("\\(");
		return new LanguageInfo().level(Level.valueOf(split[0])).metaLanguage(split.length > 1 ? split[1].substring(0, split[1].length()-1) : null);
	}

	public Level level() {
		return level;
	}

	public LanguageInfo level(Level value) {
		this.level = value;
		return this;
	}

	public String metaLanguage() {
		return metaLanguage;
	}

	public LanguageInfo metaLanguage(String metaLanguage) {
		this.metaLanguage = metaLanguage;
		return this;
	}

	@Override
	public String toString() {
		return level.name() + "(" + metaLanguage + ")";
	}
}

