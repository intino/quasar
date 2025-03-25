package io.quassar.editor.box.util;

import io.intino.alexandria.ui.services.push.UISession;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.model.Model;

public class PermissionsHelper {

	public static boolean hasPermissions(UISession session, EditorBox box) {
		return session.user() != null;
	}

	public static boolean canRemove(Model model, UISession session, EditorBox box) {
		return !ModelHelper.isMetamodel(model, box);
	}

	public static boolean canPublish(Model model, String version, UISession session, EditorBox box) {
		return !box.modelManager().isWorkspaceEmpty(model, version);
	}

}
