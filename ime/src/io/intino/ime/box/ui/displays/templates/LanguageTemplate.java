package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.components.Layer;
import io.intino.alexandria.ui.utils.DelayerUtil;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.ui.datasources.ChildrenLanguagesDatasource;
import io.intino.ime.box.ui.datasources.LanguageModelsDatasource;
import io.intino.ime.box.util.LanguageHelper;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;
import io.intino.ime.model.Release;

import java.util.UUID;

public class LanguageTemplate extends AbstractLanguageTemplate<ImeBox> {
	private Language language;
	private Release lastRelease;

	public LanguageTemplate(ImeBox box) {
		super(box);
	}

	public void language(String name) {
		this.language = box().languageManager().get(name);
		this.lastRelease = language != null ? box().languageManager().lastRelease(language) : null;
	}

	@Override
	public void init() {
		super.init();
		header.onOpenSearch(e -> openSearch());
		header.onOpenLanguage(this::open);
		header.onOpenModel(e -> openModelOf(language));
		header.onSaveSettings(e -> refresh());
		header.onChangeView(e -> refresh());
		modelDialog.onCreate(this::modelCreated);
		languageDialog.onCreate(this::languageCreated);
		addModel.onExecute(e -> openModelDialog());
		releasesBlock.onOpen(e -> refreshReleasesDialog());
		releasesCatalog.onCreateLanguage(this::openLanguageDialog);
		releasesCatalog.onCreateModel(this::openModelDialog);
		addLanguage.onExecute(e -> openLanguageDialog());
		openSearchLayerTrigger.onOpen(e -> openSearch(e.layer()));
		modelExamplesDialog.onOpen(e -> refreshModelExamplesDialog());
		modelsCatalog.onOpenModel(this::open);
		languagesExamplesDialog.onOpen(e -> refreshLanguageExamplesDialog());
		languagesCatalog.onOpenLanguage(this::open);
		languagesCatalog.onOpenModel(this::open);
	}

	@Override
	public void refresh() {
		super.refresh();
		if (language == null) {
			notifier.redirect(PathHelper.notFoundUrl(translate("Language"), session()));
			return;
		}
		header.language(language);
		header.refresh();
		logo.value(LanguageHelper.logo(language, box()));
		name.value(LanguageHelper.shortLabel(language, this::translate));
		release.title(lastRelease != null ? lastRelease.version() : ModelHelper.FirstReleaseVersion);
		description.value(language.description());
		viewModelExamples.visible(LanguageHelper.canViewExampleModels(language, lastRelease));
		viewLanguageExamples.visible(LanguageHelper.canViewExampleLanguages(language, lastRelease));
		addLanguage.visible(LanguageHelper.canCreateLanguage(language, lastRelease, user()));
		if (addLanguage.isVisible()) addLanguage.title(LanguageHelper.createLanguageLabel(language, this::translate, box()));
		addModel.visible(ModelHelper.canAddModel(lastRelease));
	}

	private void refreshReleasesDialog() {
		releasesCatalog.language(language);
		releasesCatalog.refresh();
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
		openingMessage.value(String.format(translate("Opening %s"), ModelHelper.label(model, language(), box())));
		openingBlock.show();
	}

	private void notifyOpening(Language language) {
		bodyBlock.hide();
		openingMessage.value(String.format(translate("Opening %s"), LanguageHelper.label(language, this::translate)));
		openingBlock.show();
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

	private void refreshLanguageExamplesDialog() {
		languagesExamplesDialog.title(String.format(translate("Languages defined with %s"), LanguageHelper.label(language, this::translate)));
		languagesCatalog.readonly(true);
		languagesCatalog.embedded(true);
		languagesCatalog.source(new ChildrenLanguagesDatasource(box(), session(), language));
		languagesCatalog.refresh();
	}

	private void openLanguageDialog() {
		openLanguageDialog(lastRelease);
	}

	private void openLanguageDialog(Release release) {
		languageDialog.parent(language);
		languageDialog.release(release);
		languageDialog.open();
	}

	private void openModelDialog() {
		openModelDialog(lastRelease);
	}

	private void openModelDialog(Release release) {
		modelDialog.language(language);
		modelDialog.release(release);
		modelDialog.open();
	}

	private void modelCreated(Model model) {
		open(model);
	}

	private void languageCreated(Language language) {
		open(language);
	}

}