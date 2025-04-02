package io.quassar.editor.box.util;

public class Formatters {

	public static String firstLowerCase(String value) {
		return value != null ? value.substring(0, 1).toLowerCase() + value.substring(1) : null;
	}

	public static String firstUpperCase(String value) {
		return value != null ? value.substring(0, 1).toUpperCase() + value.substring(1) : null;
	}

	public static String normalizeLanguageName(String value) {
		return Formatters.firstLowerCase(StringHelper.snakeCaseToCamelCase(StringHelper.kebabCaseToCamelCase(value)));
	}

}
