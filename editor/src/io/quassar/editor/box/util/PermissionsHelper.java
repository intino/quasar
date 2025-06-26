package io.quassar.editor.box.util;

import io.intino.alexandria.ui.services.push.UISession;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.model.*;

import java.util.List;

public class PermissionsHelper {

	public static boolean hasPermissions(UISession session, EditorBox box) {
		return session.user() != null;
	}

	public static boolean hasPermissions(Model model, String username) {
		return model.collaborators().contains(username);
	}

	public static boolean hasPermissions(Model model, UISession session, EditorBox box) {
		if (box.isDebugMode()) return true;
		if (model == null) return false;
		if (model.isPublic()) return true;
		return isOwnerOrCollaborator(model, session, box);
	}

	public static boolean hasAccessToMetamodel(Language language, UISession session, EditorBox box) {
		if (language.metamodel() == null) return false;
		Model metamodel = box.modelManager().get(language.metamodel());
		if (metamodel == null) return false;
		return isOwnerOrCollaborator(metamodel, session, box);
	}

	public static boolean hasPermissions(Language language, UISession session, EditorBox box) {
		if (language == null) return false;
		if (language.isPublic()) return true;
		if (language.isFoundational()) return true;
		if (isOwnerOrCollaborator(language, session, box)) return true;
		if (hasPermissions(box.collectionManager().get(language.collection()), session, box)) return true;
		String username = session.user() != null ? session.user().username() : null;
		return !box.modelManager().models(language, username).isEmpty();
	}

	public static boolean hasPermissions(Collection collection, UISession session, EditorBox box) {
		if (collection == null) return false;
		if (isOwnerOrCollaborator(collection, session, box)) return true;
		String username = session.user() != null ? session.user().username() : null;
		if (username == null) return false;
		return collection.anyLicense(username) != null;
	}

	public static boolean canRemove(Collection collection, UISession session, EditorBox box) {
		if (!isOwnerOrCollaborator(collection, session, box)) return false;
		return box.languageManager().languages(collection).isEmpty();
	}

	public static boolean canRemove(Model model, UISession session, EditorBox box) {
		if (!isOwnerOrCollaborator(model, session, box)) return false;
		if (!box.languageManager().exists(model)) return false;
		return !ModelHelper.isMetamodel(model, box);
	}

	public static boolean canEdit(Collection collection, UISession session, EditorBox box) {
		return isOwnerOrCollaborator(collection, session, box);
	}

	public static boolean canEdit(Language language, UISession session, EditorBox box) {
		if (language.isFoundational()) return false;
		if (!hasPermissions(language, session, box)) return false;
		String username = session.user() != null ? session.user().username() : null;
		String owner = box.languageManager().owner(language);
		if (owner != null && owner.equals(username)) return true;
		Model model = box.modelManager().get(language.metamodel());
		return model != null && canEdit(model, Model.DraftRelease, session, box);
	}

	public static boolean canAddModel(Language language, UISession session, EditorBox box) {
		if (session.user() == null) return false;
		if (language.releases().isEmpty()) return false;
		if (language.isFoundational()) return true;
		if (box.languageManager().hasAccess(language, session.user().username())) return true;
		Collection collection = box.collectionManager().get(language.collection());
		return hasPermissions(collection, session, box) || collection.subscriptionPlan() == Collection.SubscriptionPlan.Enterprise;
	}

	public static boolean canRemove(Language language, UISession session, EditorBox box) {
		if (!canEdit(language, session, box)) return false;
		return language.releases().isEmpty();
	}

	public static boolean canRemove(Language language, String release, UISession session, EditorBox box) {
		if (!canEdit(language, session, box)) return false;
		List<Model> models = box.modelManager().modelsWithRelease(language, release).stream().filter(m -> !isOwnerOrCollaborator(m, session, box)).toList();
		return models.isEmpty();
	}

	public static boolean canCheck(Model model, String version, UISession session, EditorBox box) {
		if (!hasPermissions(model, session, box)) return false;
		return !box.modelManager().isWorkspaceEmpty(model, version);
	}

	public static boolean canCommit(Model model, String version, UISession session, EditorBox box) {
		if (model.isTemplate()) return false;
		if (!hasPermissions(model, session, box)) return false;
		if (model.isExample()) return isOwnerOrCollaborator(model, session, box);
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

	public static boolean isOwnerOrCollaborator(Model model, UISession session, EditorBox box) {
		if (box.isDebugMode()) return true;
		String username = session.user() != null ? session.user().username() : null;
		if (model.owner() != null && model.owner().equals(username)) return true;
		if (model.collaborators().stream().anyMatch(c -> c.equals(username))) return true;
		Language language = box.languageManager().get(model.language());
		Model metamodel = box.modelManager().get(language.metamodel());
		return metamodel != null && isOwnerOrCollaborator(metamodel, session, box);
	}

	public static boolean isOwnerOrCollaborator(Language language, UISession session, EditorBox box) {
		String username = session.user() != null ? session.user().username() : null;
		String owner = box.languageManager().owner(language);
		if (owner != null && owner.equals(username)) return true;
		return isOwnerOrCollaborator(box.modelManager().get(language.metamodel()), session, box);
	}

	public static boolean isOwnerOrCollaborator(Collection collection, UISession session, EditorBox box) {
		String username = session.user() != null ? session.user().username() : null;
		if (collection.owner() != null && collection.owner().equals(username)) return true;
		return collection.collaborators().stream().anyMatch(c -> c.equals(username));
	}

	public static boolean canEdit(Model model, String release, UISession session, EditorBox box) {
		if (box.isDebugMode()) return model.isDraft(release);
		if (!hasPermissions(model, session, box)) return false;
		if (model.isExample()) return isOwnerOrCollaborator(model, session, box);
		if (!hasValidLicense(model.language(), session, box)) return false;
		return model.isDraft(release);
	}

	public static boolean hasValidLicense(Collection collection, UISession session, EditorBox box) {
		String username = session.user() != null ? session.user().username() : null;
		if (username == null) return false;
		if (isOwnerOrCollaborator(collection, session, box)) return true;
		return collection.validLicense(username) != null;
	}

	public static boolean hasValidLicense(Language language, UISession session, EditorBox box) {
		if (box.isDebugMode()) return true;
		if (language.isFoundational()) return true;
		return hasValidLicense(box.collectionManager().get(language.collection()), session, box);
	}

	public static boolean hasValidLicense(GavCoordinates language, UISession session, EditorBox box) {
		return hasValidLicense(box.languageManager().get(language), session, box);
	}

	public static boolean canEditSettings(Model model, String release, UISession session, EditorBox box) {
		if (!hasPermissions(model, session, box)) return false;
		return !model.isTemplate() && isOwnerOrCollaborator(model, session, box);
	}

	public static boolean canForge(Model model, Language language, String release, UISession session, EditorBox box) {
		return hasPermissions(model, session, box) && release != null;
	}

	public static boolean canLaunchExecution(Model model, Language language, String release, UISession session, EditorBox box) {
		return hasPermissions(model, session, box) && release != null;
	}

	public static boolean canOpenModel(Language language, UISession session, EditorBox box) {
		if (!canEdit(language, session, box)) return false;
		return LanguageHelper.model(language, box) != null;
	}

	public static boolean canEditTitle(Model model, EditorBox box) {
		return !model.isTemplate() && box.languageManager().getWithMetamodel(model) == null;
	}

	public static boolean canInvite(Collection collection, UISession session, EditorBox box) {
		return collection.collaborators().size() < Integer.parseInt(box.configuration().collectionCollaboratorsCount());
	}

	public static boolean hasCredit(int monthsCount, String username, EditorBox box) {
		return UserHelper.licenseTime(username, box) >= monthsCount;
	}

	public static boolean hasCredit(int monthsCount, UISession session, EditorBox box) {
		return hasCredit(monthsCount, session.user() != null ? session.user().username() : null, box);
	}

	public static boolean hasCredit(int monthsCount, Collection collection, EditorBox box) {
		return true;
	}

	public static boolean canAddLicenses(Collection collection, UISession session, EditorBox box) {
		return true;
	}

	public static boolean canRevokeLicenses(Collection collection, UISession session, EditorBox box) {
		return !collection.validLicenses().isEmpty();
	}

	public static boolean canRenew(License license, UISession session, EditorBox box) {
		return license.isExpired();
	}

	public static boolean isEnterprise(Language language, UISession session, EditorBox box) {
		if (language.isFoundational()) return true;
		return isEnterprise(box.collectionManager().get(language.collection()), session, box);
	}

	public static boolean isEnterprise(Collection collection, UISession session, EditorBox box) {
		if (collection == null) return false;
		return collection.subscriptionPlan() == Collection.SubscriptionPlan.Enterprise;
	}
}
