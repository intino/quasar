package io.intino.ime.box.ui;

import io.intino.alexandria.ui.displays.components.DateEditable;
import io.intino.alexandria.ui.displays.components.NumberEditable;
import io.intino.alexandria.ui.displays.components.TextEditable;
import io.intino.alexandria.ui.displays.components.selector.Selector;
import io.intino.alexandria.ui.services.push.UISession;
import io.intino.alexandria.ui.services.push.User;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.util.WorkspaceHelper;

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

	public static boolean checkWorkspaceName(TextEditable<?, ?> field, Function<String, String> translator, ImeBox box) {
		if (!check(field, translator)) return false;
		if (!WorkspaceHelper.validName(field.value())) { field.error("Name contains non alphanumeric characters"); return false; }
		if (WorkspaceHelper.nameInUse(field.value(), box)) { field.error("Workspace name in use"); return false; }
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

	public static io.intino.ime.model.User user(UISession session) {
		User user = session.user();
		return user != null ? new io.intino.ime.model.User(user.username(), user.fullName()) : defaultUser();
	}

	private static io.intino.ime.model.User defaultUser() {
		return new io.intino.ime.model.User("anonymous", "Anonymous");
	}

}
