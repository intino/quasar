package io.quassar.editor.box.util;

import io.quassar.editor.box.EditorBox;

import java.util.List;

public class NameHelper {

	public static boolean validName(String name) {
		return name.matches("^[a-zA-Z0-9_-]*$");
	}

	private static final List<String> ReservedNames = List.of("core", "base", "common", "shared", "system", "default", "template", "example", "sample", "internal", "package", "namespace", "type", "class", "element", "structure", "syntax", "grammar", "schema", "attribute", "operation", "rule", "statement", "expression", "object", "instance", "interface", "abstract", "entity", "function", "dsl", "forge", "model", "models", "language", "languages", "builder", "editor", "workspace", "engine", "app", "manifest", "resources", "project", "user");
	public static boolean reservedName(String name) {
		return ReservedNames.contains(name.toLowerCase());
	}

	public static boolean languageInUse(String value, EditorBox box) {
		return box.languageManager().exists(value);
	}

	public static boolean modelInUse(String value, EditorBox box) {
		return box.modelManager().exists(value);
	}

}
