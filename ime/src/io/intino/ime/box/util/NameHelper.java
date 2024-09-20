package io.intino.ime.box.util;

import io.intino.ime.box.ImeBox;

public class NameHelper {

	public static boolean validName(String name) {
		return name.matches("^[a-zA-Z0-9_-]*$");
	}

	public static boolean languageInUse(String value, ImeBox box) {
		return box.languageManager().exists(value);
	}

	public static boolean modelInUse(String value, ImeBox box) {
		return box.modelManager().exists(value);
	}


}
