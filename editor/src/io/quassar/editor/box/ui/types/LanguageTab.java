package io.quassar.editor.box.ui.types;

import java.util.Arrays;

public enum LanguageTab {
	Models, Examples;

	public static LanguageTab from(String view) {
		return Arrays.stream(LanguageTab.values()).filter(l -> l.name().equalsIgnoreCase(view)).findFirst().orElse(null);
	}
}
