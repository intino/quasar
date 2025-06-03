package io.quassar.editor.box.ui.types;

import java.util.Arrays;

public enum ForgeView {
	Info, Kit, Execution;

	public static ForgeView from(String view) {
		return Arrays.stream(ForgeView.values()).filter(l -> l.name().equalsIgnoreCase(view)).findFirst().orElse(null);
	}
}
