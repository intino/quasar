package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.Resource;
import io.intino.alexandria.ui.utils.DelayerUtil;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.LanguageCommands;
import io.intino.ime.box.commands.ModelCommands;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.ui.datasources.DatasourceHelper;
import io.intino.ime.box.ui.displays.items.LanguageMagazineItem;
import io.intino.ime.box.util.LanguageHelper;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;
import io.intino.ime.model.Release;

import java.util.List;
import java.util.function.Consumer;

public class LanguageItemTemplate extends AbstractLanguageItemTemplate<ImeBox> {
	private Language language;
	private Release selectedRelease;
	private Consumer<Model> openModelListener;

	public LanguageItemTemplate(ImeBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void onOpenLanguage(Consumer<Model> listener) {
		this.openModelListener = listener;
	}

	@Override
	public void init() {
		super.init();
		createLanguageTrigger.onOpen(e -> refreshCreateLanguageDialog());
		addModel.onExecute(e -> createModel(lastRelease()));
		addPrivateModel.onOpen(e -> refreshAddPrivateModelDialog());
		releasesDialogTrigger.onOpen(e -> refreshReleasesDialog());
	}

	@Override
	public void refresh() {
		super.refresh();
		Release release = box().languageManager().lastRelease(language);
		logo.value(LanguageHelper.logo(language, box()));
		languageTitleLink.title(language.name());
		languageTitleLink.path(PathHelper.languagePath(language));
		description.value(language.description());
		languageLink.path(PathHelper.languagePath(language));
		owner.value(language.owner());
		privatePill.visible(language.isPrivate());
		createDate.value(language.createDate());
		modelsCount.value(language.modelsCount());
		parent.value(!language.isFoundational() ? language.parent() : "-");
		createLanguageTrigger.visible(LanguageHelper.canCreateLanguage(language, release, user()));
		addModel.visible(ModelHelper.canAddModel(release) && user() == null);
		addPrivateModel.visible(ModelHelper.canAddModel(release) && user() != null);
	}

	private void refreshCreateLanguageDialog() {
		this.selectedRelease = box().languageManager().lastRelease(language);
		languageEditor.onAccept(e -> createLanguage(box().languageManager().lastRelease(language)));
		languageEditor.parent(language);
		languageEditor.reset();
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

	private void createModel(Release release) {
		createModel(release, ModelHelper.proposeName(), translate("(no name)"));
	}

	private void createModel(Release release, String name, String title) {
		open(box().commands(ModelCommands.class).create(name, title, release, DisplayHelper.user(session()), username()));
	}

	private void open(Model model) {
		openModelListener.accept(model);
		DelayerUtil.execute(this, v -> notifier.redirect(PathHelper.modelUrl(session(), model)), 600);
	}

	private void openModelOf(Language language) {
		open(box().modelManager().modelWith(language));
	}

	private Release lastRelease() {
		return box().languageManager().lastRelease(language);
	}

	private void refreshAddPrivateModelDialog() {
		this.selectedRelease = lastRelease();
		if (selectedRelease == null) return;
		languageField.value(selectedRelease.id());
		labelField.value(null);
	}

	private void refreshReleasesDialog() {
		releasesDialog.title(String.format(translate("%s releases"), language.name()));
		releasesCatalog.language(language);
		releasesCatalog.refresh();
	}

}