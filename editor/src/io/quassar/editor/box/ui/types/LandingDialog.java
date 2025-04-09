package io.quassar.editor.box.ui.types;

import java.util.Arrays;

public enum LandingDialog {
	StartModeling, Languages, Examples;

	public static LandingDialog from(String view) {
		return Arrays.stream(LandingDialog.values()).filter(l -> l.name().equalsIgnoreCase(view)).findFirst().orElse(null);
	}
}
