package io.intino.ime.box.ui;

import io.intino.alexandria.ui.displays.components.DateEditable;
import io.intino.alexandria.ui.displays.components.NumberEditable;
import io.intino.alexandria.ui.displays.components.TextEditable;
import io.intino.alexandria.ui.displays.components.selector.Selector;
import io.intino.alexandria.ui.services.push.UISession;
import io.intino.alexandria.ui.services.push.User;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.util.LanguageHelper;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.box.util.NameHelper;
import io.intino.ime.model.Model;

import java.util.function.Function;

public class DisplayHelper {

	public static boolean check(TextEditable<?, ?> field, Function<String, String> translator) {
		field.error(null);
		if (field.value() == null || field.value().isEmpty()) {
			field.error(translator.apply("Field required"));
			return false;
		}
		return true;
	}

	public static boolean checkModelName(TextEditable<?, ?> field, Function<String, String> translator, ImeBox box) {
		if (!check(field, translator)) return false;
		if (!NameHelper.validName(field.value())) { field.error("Name contains non alphanumeric characters"); return false; }
		if (NameHelper.modelInUse(field.value(), box)) { field.error("Model name in use"); return false; }
		return true;
	}

	public static boolean checkLanguageName(TextEditable<?, ?> field, Function<String, String> translator, ImeBox box) {
		if (!check(field, translator)) return false;
		if (!NameHelper.validName(field.value())) { field.error("Name contains non alphanumeric characters"); return false; }
		if (NameHelper.languageInUse(field.value(), box)) { field.error("Language name in use"); return false; }
		return true;
	}

	public static boolean check(DateEditable<?, ?> field) {
		return field.value() != null;
	}

	public static boolean check(Selector field) {
		return !field.selection().isEmpty();
	}

	public static boolean check(NumberEditable<?, ?> field) {
		return field.value() != null;
	}

	public static String user(UISession session) {
		User user = session.user();
		return user != null ? user.username() : Model.DefaultOwner;
	}

	public static void initViewMode(UISession session) {
		String preference = session.preference(ViewMode.SessionVariableName);
		if (preference != null && !preference.isEmpty()) return;
		updateViewMode(ViewMode.Languages, session);
	}

	public static void updateViewMode(ViewMode viewMode, UISession session) {
		session.add(ViewMode.SessionVariableName, viewMode.name());
	}

	public static ViewMode viewMode(UISession session) {
		String preference = session.preference(ViewMode.SessionVariableName);
		if (preference == null || preference.isEmpty()) initViewMode(session);
		return ViewMode.valueOf(session.preference(ViewMode.SessionVariableName));
	}
}
