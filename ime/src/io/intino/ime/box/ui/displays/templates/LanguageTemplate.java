package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.Resource;
import io.intino.alexandria.ui.services.push.User;
import io.intino.alexandria.ui.utils.DelayerUtil;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.LanguageCommands;
import io.intino.ime.box.commands.ModelCommands;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.util.LanguageHelper;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;
import io.intino.ime.model.Operation;
import io.intino.ime.model.Release;

import java.util.List;

public class LanguageTemplate extends AbstractLanguageTemplate<ImeBox> {
	private Language language;
	private Release lastRelease;
	private Release selectedRelease;

	public LanguageTemplate(ImeBox box) {
		super(box);
	}

	public void language(String name) {
		this.language = box().languageManager().get(name);
		this.lastRelease = box().languageManager().lastRelease(language);
	}

	@Override
	public void init() {
		super.init();
		addModel.onExecute(e -> createModel(lastRelease));
		addPrivateModelDialog.onOpen(e -> refreshAddPrivateModelDialog());
		openModel.onExecute(e -> openModelOf(language));
		createModel.onExecute(e -> createModel());
		releasesBlock.onOpen(e -> refreshReleasesDialog());
		releasesCatalog.onCreateLanguage(e -> createLanguage(e, user()));
		releasesCatalog.onCreateModel(e -> createModel(e, user()));
		settingsDialog.onOpen(e -> refreshSettingsDialog());
		saveSettings.onExecute(e -> saveSettings());
		titleField.onEnterPress(e -> createModel());
		createLanguageDialog.onOpen(e -> refreshCreateLanguageDialog());
		createLanguage.onExecute(e -> createLanguage(lastRelease));
	}

	@Override
	public void refresh() {
		super.refresh();
		header.language(language);
		header.refresh();
		logo.value(LanguageHelper.logo(language, box()));
		name.value(LanguageHelper.title(language));
		release.title(lastRelease != null ? lastRelease.version() : ModelHelper.FirstReleaseVersion);
		description.value(language.description());
		viewModelExamples.path(PathHelper.modelsPath(language));
		viewModelExamples.visible(LanguageHelper.canViewExampleModels(language, lastRelease));
		viewLanguageExamples.path(PathHelper.languagesPath(language));
		viewLanguageExamples.visible(LanguageHelper.canViewExampleLanguages(language, lastRelease));
		createLanguageTrigger.visible(LanguageHelper.canCreateLanguage(language, lastRelease, user()));
		openModel.visible(ModelHelper.canOpenModel(language, lastRelease, user()));
		addModel.visible(ModelHelper.canAddModel(lastRelease) && user() == null);
		addPrivateModel.visible(ModelHelper.canAddModel(lastRelease) && user() != null);
		viewSettings.visible(LanguageHelper.canEdit(language, lastRelease, user()));
		ownerBlock.visible(LanguageHelper.canEdit(language, lastRelease, user()));
		refreshPoweredBy();
	}

	private void refreshPoweredBy() {
		Language parent = box().languageManager().get(language.parent());
		poweredBlock.visible(parent != null);
		if (parent == null) return;
		poweredLink.path(PathHelper.languagePath(parent));
		poweredByImage.value(LanguageHelper.logo(parent, box()));
		poweredByText.value(String.format(translate("powered by %s"), LanguageHelper.title(parent)));
	}

	private void refreshAddPrivateModelDialog() {
		selectedRelease = lastRelease;
		languageField.value(selectedRelease.id());
		titleField.value(null);
	}

	private void refreshReleasesDialog() {
		releasesCatalog.language(language);
		releasesCatalog.refresh();
	}

	private void refreshSettingsDialog() {
		settingsDialog.title(String.format(translate("%s properties"), language.name()));
		settingsEditor.language(language);
		settingsEditor.refresh();
	}

	private void createModel() {
		if (!DisplayHelper.check(titleField, this::translate)) return;
		addPrivateModelDialog.close();
		createModel(selectedRelease, ModelHelper.proposeName(), titleField.value());
	}

	private void createLanguage(Release release, User user) {
		releasesBlock.close();
		if (user() == null) return;
		selectedRelease = release;
		createLanguageDialog.open();
	}

	private void createLanguage(Release release) {
		selectedRelease = release;
		createLanguage();
	}

	private void createLanguage() {
		if (!languageEditor.check()) return;
		createLanguageDialog.close();
		String name = languageEditor.name();
		String description = languageEditor.description();
		Resource logo = languageEditor.logo();
		boolean isPrivate = languageEditor.isPrivate();
		openModelOf(box().commands(LanguageCommands.class).create(name, selectedRelease, description, logo, isPrivate, username()));
	}

	private void createModel(Release release, User user) {
		releasesBlock.close();
		if (user == null) createModel(release);
		else {
			selectedRelease = release;
			addPrivateModelDialog.open();
		}
	}

	private void openModel(Release release) {
		open(box().modelManager().modelWith(release.language()));
	}

	private void createModel(Release release) {
		createModel(release, ModelHelper.proposeName(), translate("(no name)"));
	}

	private void createModel(Release release, String name, String title) {
		open(box().commands(ModelCommands.class).create(name, title, release, DisplayHelper.user(session()), username()));
	}

	private void openModelOf(Language language) {
		open(box().modelManager().modelWith(language.name()));
	}

	private void open(Model model) {
		notifyOpeningModel(model);
		DelayerUtil.execute(this, v -> notifier.redirect(PathHelper.modelUrl(session(), model)), 600);
	}

	private void notifyOpeningModel(Model model) {
		bodyBlock.hide();
		openingModelMessage.value(String.format(translate("Opening %s"), model.label()));
		searchingModelsBlock.show();
	}

	private void refreshCreateLanguageDialog() {
		languageEditor.onAccept(e -> createLanguage(lastRelease));
		languageEditor.parent(language);
		languageEditor.reset();
	}

	private void saveSettings() {
		if (!settingsEditor.check()) return;
		settingsDialog.close();
		Resource logo = settingsEditor.logo();
		String description = settingsEditor.description();
		boolean isPrivate = settingsEditor.isPrivate();
		String dockerImageUrl = settingsEditor.dockerImageUrl();
		List<Operation> operations = settingsEditor.operations();
		box().commands(LanguageCommands.class).save(language, description, isPrivate, dockerImageUrl, logo, operations, username());
		refresh();
	}

}