package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.AddItemEvent;
import io.intino.alexandria.ui.utils.DelayerUtil;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.WorkspaceCommands;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.ui.datasources.LanguagesDatasource;
import io.intino.ime.box.ui.displays.items.LanguageMagazineItem;
import io.intino.ime.box.util.WorkspaceHelper;
import io.intino.ime.model.Language;
import io.intino.ime.model.Workspace;

import java.util.function.Consumer;

public class LanguagesCatalog extends AbstractLanguagesCatalog<ImeBox> {
	private LanguagesDatasource source;
	private Consumer<Workspace> openWorkspaceListener;
	private Language selectedLanguage;

	public LanguagesCatalog(ImeBox box) {
		super(box);
	}

	public void source(LanguagesDatasource source) {
		this.source = source;
	}

	public void onOpenWorkspace(Consumer<Workspace> listener) {
		this.openWorkspaceListener = listener;
	}

	public void filter(String condition) {
		languagesMagazine.filter(condition);
	}

	@Override
	public void init() {
		super.init();
		languagesMagazine.onAddItem(this::refresh);
		addPrivateWorkspaceDialog.onOpen(e -> refreshAddPrivateWorkspaceDialog());
		createWorkspace.onExecute(e -> createWorkspace());
		nameField.onChange(e -> DisplayHelper.checkWorkspaceName(nameField, this::translate, box()));
	}

	@Override
	public void refresh() {
		super.refresh();
		languagesMagazine.source(source);
	}

	private void refresh(AddItemEvent event) {
		Language language = event.item();
		LanguageMagazineItem item = event.component();
		item.title.value(language.id());
		item.owner.value(language.owner());
		item.privatePill.visible(language.isPrivate());
		item.createDate.value(language.createDate());
		item.metaLanguage.value(language.level().metaLanguage());
		item.addWorkspace.onExecute(e -> createWorkspace(language));
		item.addWorkspace.visible(user() == null);
		item.addPrivateWorkspace.bindTo(addPrivateWorkspaceDialog);
		item.addPrivateWorkspace.onOpen(e -> refreshAddPrivateWorkspaceDialog(language));
		item.addPrivateWorkspace.visible(user() != null);
	}

	private void refreshAddPrivateWorkspaceDialog(Language language) {
		this.selectedLanguage = language;
		refreshAddPrivateWorkspaceDialog();
	}

	private void refreshAddPrivateWorkspaceDialog() {
		if (selectedLanguage == null) return;
		languageField.value(selectedLanguage.id());
		nameField.value(WorkspaceHelper.proposeName());
		titleField.value(null);
	}

	private void createWorkspace() {
		if (!DisplayHelper.checkWorkspaceName(nameField, this::translate, box())) return;
		if (!DisplayHelper.check(titleField, this::translate)) return;
		createWorkspace(selectedLanguage, nameField.value(), titleField.value());
	}

	private void createWorkspace(Language language) {
		createWorkspace(language, WorkspaceHelper.proposeName(), translate("(no name)"));
	}

	private void createWorkspace(Language language, String name, String title) {
		Workspace workspace = box().commands(WorkspaceCommands.class).create(name, title, language.id(), DisplayHelper.user(session()), username());
		openWorkspaceListener.accept(workspace);
		DelayerUtil.execute(this, v -> notifier.redirect(PathHelper.workspaceUrl(session(), workspace)), 600);
	}

}