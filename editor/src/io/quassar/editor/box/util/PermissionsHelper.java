package io.quassar.editor.box.util;

import io.intino.alexandria.ui.services.push.UISession;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.model.GavCoordinates;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

public class PermissionsHelper {

	public static boolean hasPermissions(UISession session, EditorBox box) {
		return session.user() != null;
	}

	public static boolean hasPermissions(Model model, String username) {
		return model.collaborators().contains(username);
	}

	public static boolean hasPermissions(Model model, UISession session) {
		if (model == null) return false;
		if (model.isPublic()) return true;
		return isOwnerOrCollaborator(model, session);
	}

	public static boolean hasPermissions(Language language, UISession session, EditorBox box) {
		if (language == null) return false;
		if (language.isPublic()) return true;
		if (language.isFoundational()) return true;
		String username = session.user() != null ? session.user().username() : null;
		String owner = box.languageManager().owner(language);
		if (owner != null && owner.equals(username)) return true;
		return language.grantAccessList().stream().anyMatch(a -> a.equals(owner));
	}

	public static boolean canRemove(Model model, UISession session, EditorBox box) {
		if (!hasPermissions(model, session)) return false;
		if (!box.languageManager().exists(model)) return false;
		return !ModelHelper.isMetamodel(model, box);
	}

	public static boolean canEdit(Language language, UISession session, EditorBox box) {
		if (language.isFoundational()) return false;
		return hasPermissions(language, session, box);
	}

	public static boolean canAddModel(Language language, UISession session, EditorBox box) {
		if (session.user() == null) return false;
		if (language.releases().isEmpty()) return false;
		return box.languageManager().hasAccess(language, session.user().username());
	}

	public static boolean canRemove(Language language, UISession session, EditorBox box) {
		if (!canEdit(language, session, box)) return false;
		return box.modelManager().models(language.name()).isEmpty();
	}

	public static boolean canCheck(Model model, String version, UISession session, EditorBox box) {
		if (!hasPermissions(model, session)) return false;
		return !box.modelManager().isWorkspaceEmpty(model, version);
	}

	public static boolean canCommit(Model model, String version, UISession session, EditorBox box) {
		if (model.isTemplate()) return false;
		if (!hasPermissions(model, session)) return false;
		if (model.isExample()) return isOwnerOrCollaborator(model, session);
		return !box.modelManager().isWorkspaceEmpty(model, version);
	}

	public static boolean canClone(Model model, String version, UISession session, EditorBox box) {
		if (model.isTemplate()) return false;
		if (session.user() == null) return false;
		if (!model.isPublic() && !isOwner(model, session)) return false;
		return !box.modelManager().isWorkspaceEmpty(model, version);
	}

	public static boolean canEditTemplate(GavCoordinates languageCoordinates, UISession session, EditorBox box) {
		Language language = box.languageManager().get(languageCoordinates);
		if (session.user() == null) return false;
		if (!hasPermissions(language, session, box)) return false;
		return language.release(languageCoordinates.version()) != null;
	}

	private static boolean isOwner(Model model, UISession session) {
		if (model.owner() == null) return false;
		String username = session.user() != null ? session.user().username() : null;
		return model.owner().equalsIgnoreCase(username);
	}

	public static boolean isOwnerOrCollaborator(Model model, UISession session) {
		String username = session.user() != null ? session.user().username() : null;
		if (model.owner() != null && model.owner().equals(username)) return true;
		return model.collaborators().stream().anyMatch(c -> c.equals(username));
	}

	public static boolean canEdit(Model model, String release, UISession session) {
		if (!hasPermissions(model, session)) return false;
		if (model.isExample()) return isOwnerOrCollaborator(model, session);
		return model.isDraft(release);
	}

	public static boolean canEditSettings(Model model, String release, UISession session) {
		if (!hasPermissions(model, session)) return false;
		return !model.isExample() || isOwnerOrCollaborator(model, session);
	}

	public static boolean canForge(Model model, Language language, String release, UISession session) {
		return hasPermissions(model, session) && release != null && !release.equals(Model.DraftRelease);
	}

	public static boolean canLaunchExecution(Model model, Language language, String release, UISession session) {
		return hasPermissions(model, session) && release != null && !release.equals(Model.DraftRelease);
	}

	public static boolean canOpenModel(Language language, UISession session, EditorBox box) {
		if (!canEdit(language, session, box)) return false;
		return LanguageHelper.model(language, box) != null;
	}

	public static boolean canEditTitle(Model model, EditorBox box) {
		return !model.isTemplate() && box.languageManager().getWithMetamodel(model) == null;
	}
}
