package io.quassar.editor.box.ui.types;

import java.util.Arrays;

public enum LanguagesTab {
	PublicLanguages, OwnerLanguages;

	public static LanguagesTab from(String view) {
		return Arrays.stream(LanguagesTab.values()).filter(l -> l.name().equalsIgnoreCase(view)).findFirst().orElse(null);
	}
}
