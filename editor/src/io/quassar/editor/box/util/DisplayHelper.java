package io.quassar.editor.box.util;

import io.intino.alexandria.Resource;
import io.intino.alexandria.ui.displays.components.TextEditable;
import io.intino.alexandria.ui.services.push.UISession;
import io.intino.alexandria.ui.services.push.User;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.io.ByteArrayInputStream;
import java.util.function.Function;

public class DisplayHelper {

	public static String user(UISession session) {
		User user = session.user();
		return user != null ? user.username() : Model.DefaultOwner;
	}

	public static boolean check(TextEditable<?, ?> field, Function<String, String> translator) {
		field.error(null);
		if (field.value() == null || field.value().isEmpty()) {
			field.error(translator.apply("Field required"));
			return false;
		}
		return true;
	}

	public static boolean checkLanguageName(TextEditable<?, ?> field, Language language, Function<String, String> translator, EditorBox box) {
		if (!check(field, translator)) return false;
		if (!NameHelper.validName(field.value())) { field.error("Name contains non alphanumeric characters"); return false; }
		if (NameHelper.languageInUse(field.value(), box)) { field.error("Already exists a language with that name"); return false; }
		if (NameHelper.modelInUse(language, field.value(), box)) { field.error("Already exists a language with that name"); return false; }
		return true;
	}

	public static Resource emptyFile() {
		return new Resource("empty", new ByteArrayInputStream(new byte[0]));
	}

}
