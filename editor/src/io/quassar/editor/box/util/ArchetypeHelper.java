package io.quassar.editor.box.util;

import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

public class ArchetypeHelper {

	public static String relativePath(Model model) {
		return relativeModelPath(model.id());
	}

	public static String relativeModelPath(String id) {
		if (!id.contains("-")) return id;
		int pos = id.indexOf("-");
		return id.substring(0, pos);
	}

	public static String languageDirectoryName(String name) {
		return name.replace("." + Language.QuassarGroup, "");
	}

}
