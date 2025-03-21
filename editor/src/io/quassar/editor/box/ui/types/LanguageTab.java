package io.quassar.editor.box.ui.types;

import java.util.Arrays;

public enum LanguageTab {
	Home, Models;

	public static LanguageTab from(String tab) {
		return Arrays.stream(LanguageTab.values()).filter(l -> l.name().equalsIgnoreCase(tab)).findFirst().orElse(null);
	}
}
