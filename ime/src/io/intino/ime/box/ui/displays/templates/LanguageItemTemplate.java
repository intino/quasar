package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.utils.DelayerUtil;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.util.LanguageHelper;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;
import io.intino.ime.model.Release;

import java.util.function.Consumer;

public class LanguageItemTemplate extends AbstractLanguageItemTemplate<ImeBox> {
	private Language language;
	private Consumer<Language> openLanguageListener;
	private Consumer<Model> openModelListener;

	public LanguageItemTemplate(ImeBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void onOpenModel(Consumer<Model> listener) {
		this.openModelListener = listener;
	}

	public void onOpenLanguage(Consumer<Language> listener) {
		this.openLanguageListener = listener;
	}

	@Override
	public void init() {
		super.init();
		languageDialog.onCreate(this::languageCreated);
		modelDialog.onCreate(this::modelCreated);
		addLanguage.onExecute(e -> openLanguageDialog());
		addModel.onExecute(e -> openModelDialog());
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
		addLanguage.visible(LanguageHelper.canCreateLanguage(language, release, user()));
		addModel.visible(ModelHelper.canAddModel(release));
	}

	private Release lastRelease() {
		return box().languageManager().lastRelease(language);
	}

	private void refreshReleasesDialog() {
		releasesDialog.title(String.format(translate("%s releases"), language.name()));
		releasesCatalog.language(language);
		releasesCatalog.refresh();
	}

	private void openLanguageDialog() {
		languageDialog.parent(language);
		languageDialog.release(lastRelease());
		languageDialog.open();
	}

	private void languageCreated(Language language) {
		open(language);
	}

	private void open(Language language) {
		if (openLanguageListener != null) openLanguageListener.accept(language);
		DelayerUtil.execute(this, v -> notifier.redirect(PathHelper.languageUrl(session(), language)), 600);
	}

	private void openModelDialog() {
		modelDialog.language(language);
		modelDialog.release(lastRelease());
		modelDialog.open();
	}

	private void modelCreated(Model model) {
		open(model);
	}

	private void open(Model model) {
		if (openModelListener != null) openModelListener.accept(model);
		DelayerUtil.execute(this, v -> notifier.redirect(PathHelper.modelUrl(session(), model)), 600);
	}

}