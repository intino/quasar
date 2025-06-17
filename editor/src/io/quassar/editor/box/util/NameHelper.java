package io.quassar.editor.box.util;

import io.quassar.editor.box.EditorBox;

import java.util.List;

public class NameHelper {

	public static boolean validName(String name) {
		if (name == null || name.endsWith(".")) return false;
		return name.matches("^[a-zA-Z0-9_-]*$");
	}

	private static final List<String> ReservedCollectionNames = List.of("collection", "quassar", "monentia");
	public static boolean reservedCollectionName(String name) {
		return ReservedCollectionNames.contains(name.toLowerCase());
	}

	private static final List<String> ReservedNames = List.of("core", "base", "common", "shared", "system", "default", "template", "example", "sample", "internal", "package", "namespace", "type", "class", "element", "structure", "syntax", "grammar", "schema", "attribute", "operation", "rule", "statement", "expression", "object", "instance", "interface", "abstract", "entity", "function", "dsl", "forge", "model", "models", "language", "languages", "builder", "editor", "workspace", "engine", "app", "manifest", "resources", "project", "user");
	public static boolean reservedName(String name) {
		return ReservedNames.contains(name.toLowerCase());
	}

	public static boolean collectionInUse(String name, EditorBox box) {
		return box.collectionManager().exists(name);
	}

	public static boolean languageInUse(String collection, String name, EditorBox box) {
		return box.languageManager().exists(collection, name);
	}

	public static boolean modelInUse(String name, EditorBox box) {
		return box.modelManager().exists(name);
	}

}
