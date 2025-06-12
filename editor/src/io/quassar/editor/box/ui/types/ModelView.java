package io.quassar.editor.box.ui.types;

import java.util.Arrays;

public enum ModelView {
	Model, Resources;

	public static ModelView from(String view) {
		return Arrays.stream(ModelView.values()).filter(l -> l.name().equalsIgnoreCase(view)).findFirst().orElse(null);
	}
}
