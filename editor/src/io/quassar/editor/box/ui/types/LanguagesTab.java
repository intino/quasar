package io.quassar.editor.box.ui.types;

import java.util.Arrays;

public enum LanguagesTab {
	Home, Languages;

	public static LanguagesTab from(String tab) {
		return Arrays.stream(LanguagesTab.values()).filter(l -> l.name().equalsIgnoreCase(tab)).findFirst().orElse(null);
	}
}
