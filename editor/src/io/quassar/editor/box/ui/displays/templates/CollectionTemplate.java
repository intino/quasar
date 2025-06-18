package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.CollectionCommands;
import io.quassar.editor.box.ui.datasources.CollectionLanguagesDatasource;
import io.quassar.editor.box.ui.datasources.LanguagesDatasource;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.box.util.SubjectHelper;
import io.quassar.editor.model.*;

import java.util.List;

public class CollectionTemplate extends AbstractCollectionTemplate<EditorBox> {
	private Collection collection;

	public CollectionTemplate(EditorBox box) {
		super(box);
	}

	public void open(String collection) {
		this.collection = box().collectionManager().get(collection);
		refresh();
	}

	@Override
	public void init() {
		super.init();
		mainBlock.onInit(e -> initMainBlock());
		mainBlock.onShow(e -> refreshMainBlock());
	}

	@Override
	public void refresh() {
		super.refresh();
		notFoundBlock.visible(!PermissionsHelper.hasPermissions(collection, session(), box()));
		refreshHeader();
		refreshBlocks();
	}

	private void refreshHeader() {
		headerStamp.visible(PermissionsHelper.hasPermissions(collection, session(), box()));
		if (!headerStamp.isVisible()) return;
		headerStamp.collection(collection);
		headerStamp.refresh();
	}

	private void refreshBlocks() {
		mainBlock.visible(PermissionsHelper.hasPermissions(collection, session(), box()));
	}

	private void initMainBlock() {
		remove.onExecute(e -> removeCollection());
		remove.signChecker((sign, reason) -> collection.name().equals(sign));
		collaboratorsStamp.onChange(this::saveCollaborators);
	}

	private void refreshMainBlock() {
		refreshProperties();
		refreshLanguages();
		refreshCollaborators();
		refreshLicenses();
	}

	private void refreshProperties() {
		remove.visible(PermissionsHelper.canRemove(collection, session(), box()));
		subscriptionPlan.value(translate("%s subscription").formatted(translate(collection.subscriptionPlan().name())));
		subscriptionPlan.backgroundColor(subscriptionPlanBackgroundColor());
	}

	private void refreshLanguages() {
		languagesCatalog.collection(collection);
		languagesCatalog.refresh();
	}

	private void refreshCollaborators() {
		collaboratorsStamp.owner(collection.owner());
		collaboratorsStamp.collaborators(collection.collaborators());
		collaboratorsStamp.readonly(!PermissionsHelper.canInvite(collection, session(), box()));
		collaboratorsStamp.collaboratorsTitle.value(translate("Collaborators (max. %s)").formatted(box().configuration().collectionCollaboratorsCount()));
		collaboratorsStamp.refresh();
	}

	private void refreshLicenses() {
		licensesStamp.collection(collection);
		licensesStamp.refresh();
	}

	private String subscriptionPlanBackgroundColor() {
		if (collection.subscriptionPlan() == Collection.SubscriptionPlan.Enterprise) return "black";
		return "blue";
	}

	private void removeCollection() {
		box().commands(CollectionCommands.class).remove(collection, username());
		notifier.redirect(PathHelper.homeUrl(session()));
	}

	private void saveCollaborators(List<String> collaborators) {
		box().commands(CollectionCommands.class).save(collection, collaborators, username());
		refreshCollaborators();
	}

}