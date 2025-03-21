package io.quassar.editor.box.ui.types;

import java.util.Arrays;

public enum LanguagesView {
	PublicLanguages, OwnerLanguages;

	public static LanguagesView from(String view) {
		return Arrays.stream(LanguagesView.values()).filter(l -> l.name().equalsIgnoreCase(view)).findFirst().orElse(null);
	}
}
