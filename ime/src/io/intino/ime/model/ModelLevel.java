package io.intino.ime.model;

public enum ModelLevel {
	M1("Level 1"), M2("Level 2"), M3("Level 3");

	private final String label;

	ModelLevel(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}
}