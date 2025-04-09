package io.quassar.editor.box.util;

import io.intino.alexandria.Resource;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.displays.components.TextEditable;
import io.intino.alexandria.ui.server.UIFile;
import io.intino.alexandria.ui.services.push.UISession;
import io.intino.alexandria.ui.services.push.User;
import io.quassar.editor.box.EditorBox;

import java.io.*;
import java.util.function.Function;

public class DisplayHelper {

	public static long MinItemsCount = 0;

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

	public static boolean checkLanguageName(TextEditable<?, ?> field, Function<String, String> translator, EditorBox box) {
		if (!check(field, translator)) return false;
		if (!NameHelper.validName(field.value())) { field.error("Name contains non alphanumeric characters"); return false; }
		if (NameHelper.reservedName(field.value())) { field.error("This name is reserved and cannot be used."); return false; }
		if (NameHelper.languageInUse(field.value(), box)) { field.error("Already exists a language with that name"); return false; }
		if (NameHelper.modelInUse(field.value(), box)) { field.error("Already exists a model with that name"); return false; }
		return true;
	}

	public static Resource emptyFile() {
		return new Resource("empty", new ByteArrayInputStream(new byte[0]));
	}

	public static UIFile uiFile(String label, File content) {
		try {
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
