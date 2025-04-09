package io.quassar.editor.box.ui.types;

import java.util.Arrays;

public enum LanguageView {
	Help, About;

	public static LanguageView from(String view) {
		return Arrays.stream(LanguageView.values()).filter(l -> l.name().equalsIgnoreCase(view)).findFirst().orElse(null);
	}
}
