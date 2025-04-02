package io.quassar.editor.box.util;

import io.intino.alexandria.ui.services.push.UISession;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.model.Model;

public class PermissionsHelper {

	public static boolean hasPermissions(UISession session, EditorBox box) {
		return session.user() != null;
	}

	public static boolean hasPermissions(Model model, String username) {
		return model.collaborators().contains(username);
	}

	public static boolean canRemove(Model model, UISession session, EditorBox box) {
		return !ModelHelper.isMetamodel(model, box);
	}

	public static boolean canBuild(Model model, String version, UISession session, EditorBox box) {
		return !box.modelManager().isWorkspaceEmpty(model, version);
	}

	public static boolean canPublish(Model model, String version, UISession session, EditorBox box) {
		return !box.modelManager().isWorkspaceEmpty(model, version);
	}

	public static boolean canClone(Model model, String version, UISession session, EditorBox box) {
		if (!model.isPublic() && !isOwner(model, session)) return false;
		return !box.modelManager().isWorkspaceEmpty(model, version);
	}

	private static boolean isOwner(Model model, UISession session) {
		if (model.owner() == null) return false;
		String username = session.user() != null ? session.user().username() : null;
		return model.owner().equalsIgnoreCase(username);
	}

	public static boolean canEdit(Model model, String release) {
		return model.isDraft(release);
	}

}
