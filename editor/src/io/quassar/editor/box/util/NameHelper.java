package io.quassar.editor.box.util;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.model.Language;

public class NameHelper {

	public static boolean validName(String name) {
		return name.matches("^[a-zA-Z0-9_-]*$");
	}

	public static boolean languageInUse(String value, EditorBox box) {
		return box.languageManager().exists(value);
	}

	public static boolean modelInUse(Language language, String value, EditorBox box) {
		return box.modelManager().exists(language, value);
	}

}
