package io.quassar.editor.box.util;

import io.intino.alexandria.Resource;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.displays.components.TextEditable;
import io.intino.alexandria.ui.server.UIFile;
import io.intino.alexandria.ui.services.push.UISession;
import io.intino.alexandria.ui.services.push.User;
import io.intino.builderservice.schemas.Message;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.builder.CheckResult;

import java.io.*;
import java.util.function.Function;

public class DisplayHelper {

	public static long MinItemsCount = 5;

	public static String valueOrDefault(String value) {
		return value != null && !value.isEmpty() ? value : "-";
	}

	public static String user(UISession session) {
		User user = session.user();
		return user != null ? user.username() : io.quassar.editor.model.User.Anonymous;
	}

	public static boolean check(TextEditable<?, ?> field, Function<String, String> translator) {
		field.error(null);
		if (field.value() == null || field.value().isEmpty()) {
			field.error(translator.apply("Field required"));
			return false;
		}
		return true;
	}

	public record CheckResult(boolean success, String message) {}
	public static CheckResult checkCollectionName(String name, Function<String, String> translator, EditorBox box) {
		if (name == null || name.isEmpty()) return new CheckResult(false, translator.apply("Collection name is required"));
		if (!NameHelper.validName(name)) return new CheckResult(false, translator.apply("Collection name contains non alphanumeric characters"));
		if (NameHelper.reservedCollectionName(name)) return new CheckResult(false, translator.apply("Collection name is reserved and cannot be used"));
		if (NameHelper.collectionInUse(name, box)) return new CheckResult(false, translator.apply("Collection is already registered. You need to be invited as author in this collection"));
		return new CheckResult(true, null);
	}


	public static CheckResult checkLanguageName(String name, Function<String, String> translator) {
		if (name == null || name.isEmpty()) return new CheckResult(false, translator.apply("Name is required"));
		if (!NameHelper.validName(name)) return new CheckResult(false, translator.apply("Name contains non alphanumeric characters"));
		if (NameHelper.reservedName(name)) return new CheckResult(false, translator.apply("This name is reserved and cannot be used"));
		return new CheckResult(true, null);
	}

	public static CheckResult checkLanguageInUse(String collection, String name, Function<String, String> translator, EditorBox box) {
		if (NameHelper.languageInUse(collection, name, box)) return new CheckResult(false, translator.apply("A DSL with this name already exists in the selected collection"));
		return new CheckResult(true, null);
	}

	public static Resource emptyFile() {
		return new Resource("empty", new ByteArrayInputStream(new byte[0]));
	}

	public static UIFile uiFile(String label, File content) {
		try {
			if (!content.exists()) return uiFile(label, new ByteArrayInputStream(new byte[0]));
			return uiFile(label, new FileInputStream(content));
		} catch (FileNotFoundException e) {
			Logger.error(e);
			return uiFile(label, new ByteArrayInputStream(new byte[0]));
		}
	}

	public static UIFile uiFile(String label, InputStream content) {
		return new UIFile() {
			@Override
			public String label() {
				return label;
			}

			@Override
			public InputStream content() {
				return content;
			}
		};
	}

}
