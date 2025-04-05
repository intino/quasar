package io.quassar.editor.box.util;

import io.intino.alexandria.ui.services.push.UISession;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

public class PermissionsHelper {

	public static boolean hasPermissions(UISession session, EditorBox box) {
		return session.user() != null;
	}

	public static boolean hasPermissions(Model model, String username) {
		return model.collaborators().contains(username);
	}

	private static boolean hasPermissions(Model model, UISession session) {
		String username = session.user() != null ? session.user().username() : null;
		if (model.owner() != null && model.owner().equals(username)) return true;
		return model.collaborators().stream().anyMatch(c -> c.equals(username));
	}

	public static boolean canRemove(Model model, UISession session, EditorBox box) {
		if (!hasPermissions(model, session)) return false;
		if (box.languageManager().exists(model.name())) return false;
		return !ModelHelper.isMetamodel(model, box);
	}

	public static boolean canEdit(Language language, UISession session, EditorBox box) {
		if (language.isFoundational()) return false;
		Language parentLanguage = box.languageManager().get(language.parent());
		if (parentLanguage == null) return false;
		Model model = box.modelManager().get(parentLanguage, language.name());
		if (model == null) return false;
		return hasPermissions(model, session);
	}

	public static boolean canRemove(Language language, UISession session, EditorBox box) {
		if (!canEdit(language, session, box)) return false;
		return box.modelManager().models(language.name()).isEmpty();
	}

	public static boolean canBuild(Model model, String version, UISession session, EditorBox box) {
		if (!hasPermissions(model, session)) return false;
		return !box.modelManager().isWorkspaceEmpty(model, version);
	}

	public static boolean canDeploy(Model model, String version, UISession session, EditorBox box) {
		if (model.isTemplate()) return false;
		if (!hasPermissions(model, session)) return false;
		return !box.modelManager().isWorkspaceEmpty(model, version);
	}

	public static boolean canClone(Model model, String version, UISession session, EditorBox box) {
		if (model.isTemplate()) return false;
		if (session.user() == null) return false;
		if (!model.isPublic() && !isOwner(model, session)) return false;
		return !box.modelManager().isWorkspaceEmpty(model, version);
	}

	public static boolean canEditTemplate(Model model, String version, UISession session, EditorBox box) {
		if (session.user() == null) return false;
		if (!isOwner(model, session)) return false;
		Language language = box.languageManager().get(model.name());
		if (language == null) return false;
		return box.modelManager().exists(language, Model.Template);
	}

	private static boolean isOwner(Model model, UISession session) {
		if (model.owner() == null) return false;
		String username = session.user() != null ? session.user().username() : null;
		return model.owner().equalsIgnoreCase(username);
	}

	public static boolean canEdit(Model model, String release, UISession session) {
		if (!hasPermissions(model, session)) return false;
		return model.isDraft(release);
	}

	public static boolean canEditSettings(Model model, String release, UISession session) {
		return hasPermissions(model, session);
	}

	public static boolean canOpenModel(Language language, UISession session, EditorBox box) {
		if (!canEdit(language, session, box)) return false;
		return LanguageHelper.model(language, box) != null;
	}
}
