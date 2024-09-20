package io.intino.ime.model;

public enum LanguageLevel {
	L1("Level 1"), L2("Level 2"), L3("Level 3");

	private final String label;

	LanguageLevel(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}
}