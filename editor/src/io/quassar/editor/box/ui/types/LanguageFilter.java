package io.quassar.editor.box.ui.types;

import java.util.Arrays;

public enum LanguageFilter {
	Tag, Owner;

	public static LanguageFilter from(String tab) {
		return Arrays.stream(LanguageFilter.values()).filter(l -> l.name().equalsIgnoreCase(tab)).findFirst().orElse(null);
	}
}
