package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.Resource;
import io.intino.alexandria.ui.displays.components.Layer;
import io.intino.alexandria.ui.services.push.User;
import io.intino.alexandria.ui.utils.DelayerUtil;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.LanguageCommands;
import io.intino.ime.box.commands.ModelCommands;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.ui.datasources.LanguageModelsDatasource;
import io.intino.ime.box.ui.datasources.ChildrenLanguagesDatasource;
import io.intino.ime.box.util.LanguageHelper;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;
import io.intino.ime.model.Release;

import java.util.UUID;

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
		header.onOpenSearch(e -> openSearch());
		header.onOpenLanguage(this::open);
		header.onOpenModel(e -> openModelOf(language));
		header.onSaveSettings(e -> refresh());
		header.onChangeView(e -> refresh());
		addModel.onExecute(e -> createModel(lastRelease));
		addPrivateModelDialog.onOpen(e -> refreshAddPrivateModelDialog());
		createModel.onExecute(e -> createModel());
		releasesBlock.onOpen(e -> refreshReleasesDialog());
		releasesCatalog.onCreateLanguage(e -> createLanguage(e, user()));
		releasesCatalog.onCreateModel(e -> createModel(e, user()));
		titleField.onEnterPress(e -> createModel());
		createLanguageDialog.onOpen(e -> refreshCreateLanguageDialog());
		createLanguage.onExecute(e -> createLanguage(lastRelease));
		openSearchLayerTrigger.onOpen(e -> openSearch(e.layer()));
		modelExamplesDialog.onOpen(e -> refreshModelExamplesDialog());
		modelsCatalog.onOpenModel(this::open);
		languagesExamplesDialog.onOpen(e -> refreshlanguageExamplesDialog());
		languagesCatalog.onOpenModel(this::open);
	}

	@Override
	public void refresh() {
		super.refresh();
		header.language(language);
		header.refresh();
		logo.value(LanguageHelper.logo(language, box()));
		name.value(LanguageHelper.shortLabel(language, this::translate));
		release.title(lastRelease != null ? lastRelease.version() : ModelHelper.FirstReleaseVersion);
		description.value(language.description());
		viewModelExamples.visible(LanguageHelper.canViewExampleModels(language, lastRelease));
		viewLanguageExamples.visible(LanguageHelper.canViewExampleLanguages(language, lastRelease));
		createLanguageTrigger.visible(LanguageHelper.canCreateLanguage(language, lastRelease, user()));
		if (createLanguageTrigger.isVisible()) createLanguageTrigger.title(LanguageHelper.createLanguageLabel(language, this::translate, box()));
		addModel.visible(ModelHelper.canAddModel(lastRelease) && user() == null);
		addPrivateModel.visible(ModelHelper.canAddModel(lastRelease) && user() != null);
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
		open(box().commands(LanguageCommands.class).create(name, selectedRelease, description, logo, isPrivate, username()));
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

	private void open(Language language) {
		notifyOpening(language);
		DelayerUtil.execute(this, v -> notifier.redirect(PathHelper.languageUrl(session(), language)), 600);
	}

	private void open(Model model) {
		modelExamplesDialog.close();
		languagesExamplesDialog.close();
		notifyOpening(model);
		DelayerUtil.execute(this, v -> notifier.redirect(PathHelper.modelUrl(session(), model)), 600);
	}

	private void notifyOpening(Model model) {
		bodyBlock.hide();
		openingModelMessage.value(String.format(translate("Opening %s"), ModelHelper.label(model, language(), box())));
		searchingModelsBlock.show();
	}

	private void notifyOpening(Language language) {
		bodyBlock.hide();
		openingModelMessage.value(String.format(translate("Opening %s"), LanguageHelper.label(language, this::translate)));
		searchingModelsBlock.show();
	}

	private void refreshCreateLanguageDialog() {
		createLanguageDialog.title(LanguageHelper.createLanguageLabel(language, this::translate, box()));
		languageEditor.onAccept(e -> createLanguage(lastRelease));
		languageEditor.parent(language);
		languageEditor.reset();
	}

	private void openSearch() {
		openSearchLayerTrigger.address(path -> PathHelper.searchPath());
		openSearchLayerTrigger.launch();
	}

	private void openSearch(Layer<?, ?> layer) {
		HomeTemplate template = new HomeTemplate(box());
		template.id(UUID.randomUUID().toString());
		layer.template(template);
		template.page(HomeTemplate.Page.Search);
		template.refresh();
	}

	private void refreshModelExamplesDialog() {
		modelExamplesDialog.title(String.format(translate("Models created with %s"), LanguageHelper.label(language, this::translate)));
		modelsCatalog.readonly(true);
		modelsCatalog.embedded(true);
		modelsCatalog.language(language);
		modelsCatalog.source(new LanguageModelsDatasource(box(), session(), language));
		modelsCatalog.refresh();
	}

	private void refreshlanguageExamplesDialog() {
		languagesExamplesDialog.title(String.format(translate("Languages defined with %s"), LanguageHelper.label(language, this::translate)));
		languagesCatalog.readonly(true);
		languagesCatalog.embedded(true);
		languagesCatalog.source(new ChildrenLanguagesDatasource(box(), session(), language));
		languagesCatalog.refresh();
	}

}