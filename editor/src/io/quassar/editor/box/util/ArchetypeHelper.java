package io.quassar.editor.box.util;

import io.quassar.editor.model.Model;

public class ArchetypeHelper {

	public static String relativePath(Model model) {
		return relativeModelPath(model.id());
	}

	public static String relativeModelPath(String id) {
		if (id == null) return null;
		return id.length() > 4 ? id.substring(0, 4) : id;
	}

}
