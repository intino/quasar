package io.intino.languageeditor.box.ui;

import io.intino.alexandria.ui.displays.components.DateEditable;
import io.intino.alexandria.ui.displays.components.NumberEditable;
import io.intino.alexandria.ui.displays.components.TextEditable;
import io.intino.alexandria.ui.displays.components.selector.Selector;

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

	public static boolean check(DateEditable<?, ?> field) {
		return field.value() != null;
	}

	public static boolean check(Selector field) {
		return field.selection().size() > 0;
	}

	public static boolean check(NumberEditable<?, ?> field) {
		return field.value() != null;
	}

	}
