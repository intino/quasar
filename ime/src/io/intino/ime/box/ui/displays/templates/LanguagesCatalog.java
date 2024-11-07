package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.AddCollectionItemEvent;
import io.intino.alexandria.ui.utils.DelayerUtil;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.ui.datasources.LanguagesDatasource;
import io.intino.ime.box.ui.displays.items.LanguageMagazineItem;
import io.intino.ime.box.ui.model.Owner;
import io.intino.ime.box.ui.model.SearchItem;
import io.intino.ime.box.ui.model.Tag;
import io.intino.ime.box.util.LanguageHelper;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;
import io.intino.ime.model.Release;

import java.util.List;
import java.util.function.Consumer;

public class LanguagesCatalog extends AbstractLanguagesCatalog<ImeBox> {
	private String _title = null;
	private LanguagesDatasource source;
	private Consumer<Language> openLanguageListener;
	private Consumer<Model> openModelListener;
	private boolean readonly = false;
	private boolean embedded = false;
	private Language selectedLanguage;

	public LanguagesCatalog(ImeBox box) {
		super(box);
	}

	public void title(String title) {
		this._title = title;
	}

	public void source(LanguagesDatasource source) {
		this.source = source;
	}

	public void onOpenLanguage(Consumer<Language> listener) {
		this.openLanguageListener = listener;
	}

	public void onOpenModel(Consumer<Model> listener) {
		this.openModelListener = listener;
	}

	public void readonly(boolean value) {
		this.readonly = value;
	}

	public void embedded(boolean value) {
		this.embedded = value;
	}

	public long itemCount() {
		return languagesMagazine.itemCount();
	}

	public void filter(SearchItem item) {
		LanguagesDatasource source = languagesMagazine.source();
		source.tag(item instanceof Tag ? item.name() : null);
		source.owner(item instanceof Owner ? item.name() : null);
		languagesMagazine.reload();
	}

	public void filter(String condition) {
		languagesMagazine.filter(condition);
	}

	public void filter(String grouping, List<String> values) {
		languagesMagazine.filter(grouping, values);
	}

	@Override
	public void init() {
		super.init();
		languagesMagazine.onAddItem(this::refresh);
		languageDialog.onCreate(this::languageCreated);
		modelDialog.onCreate(this::modelCreated);
		releasesDialog.onOpen(e -> refreshReleasesDialog());
		releasesCatalog.onCreateLanguage(this::openLanguageDialog);
		releasesCatalog.onCreateModel(this::openModelDialog);
		searchBox.onChange(e -> filter((String) e.value()));
		searchBox.onEnterPress(e -> filter((String) e.value()));
	}

	@Override
	public void refresh() {
		super.refresh();
		title.visible(_title != null);
		toolbar.visible(embedded);
		if (title.isVisible()) title.value(translate(_title));
		searchBox.visible(embedded);
		languagesMagazine.source(source);
	}

	private void refresh(AddCollectionItemEvent event) {
		Language language = event.item();
		Release release = box().languageManager().lastRelease(language);
		LanguageMagazineItem item = event.component();
		item.logo.value(LanguageHelper.logo(language, box()));
		item.languageTitleLink.title(language.name());
		item.languageTitleLink.path(PathHelper.languagePath(language));
		item.description.value(language.description());
		item.languageLink.path(PathHelper.languagePath(language));
		item.owner.value(language.owner());
		item.privatePill.visible(language.isPrivate());
		item.createDate.value(language.createDate());
		item.modelsCount.value(language.modelsCount());
		item.parent.value(!language.isFoundational() ? language.parent() : "-");
		item.addLanguage.visible(LanguageHelper.canCreateLanguage(language, release, user()));
		item.addLanguage.onExecute(e -> openLanguageDialog(language));
		item.addModel.visible(!readonly && ModelHelper.canAddModel(release));
		item.addModel.onExecute(e -> openModelDialog(language));
		item.releasesDialogTrigger.bindTo(releasesDialog);
		item.releasesDialogTrigger.onOpen(e -> refreshReleasesDialog(language));
	}

	private void openLanguageDialog(Release release) {
		releasesDialog.close();
		openLanguageDialog(selectedLanguage, release);
	}

	private void openLanguageDialog(Language language) {
		openLanguageDialog(language, lastRelease(language));
	}

	private void openLanguageDialog(Language language, Release release) {
		languageDialog.parent(language);
		languageDialog.release(release);
		languageDialog.open();
	}

	private void openModelDialog(Language language) {
		openModelDialog(language, lastRelease(language));
	}

	private void openModelDialog(Release release) {
		releasesDialog.close();
		openModelDialog(selectedLanguage, release);
	}

	private void openModelDialog(Language language, Release release) {
		modelDialog.language(language);
		modelDialog.release(release);
		modelDialog.open();
	}

	private void refreshReleasesDialog(Language language) {
		this.selectedLanguage = language;
		refreshReleasesDialog();
	}

	private void refreshReleasesDialog() {
		if (selectedLanguage == null) return;
		releasesDialog.title(String.format(translate("%s releases"), selectedLanguage.name()));
		releasesCatalog.readonly(readonly);
		releasesCatalog.language(selectedLanguage);
		releasesCatalog.refresh();
	}

	private void languageCreated(Language language) {
		open(language);
	}

	private void open(Language language) {
		if (openLanguageListener != null) openLanguageListener.accept(language);
		DelayerUtil.execute(this, v -> notifier.redirect(PathHelper.languageUrl(session(), language)), 600);
	}

	private void modelCreated(Model model) {
		open(model);
	}

	private void openModelOf(Language language) {
		open(box().modelManager().modelWith(language));
	}

	private void open(Model model) {
		if (openModelListener != null) openModelListener.accept(model);
		DelayerUtil.execute(this, v -> notifier.redirect(PathHelper.modelUrl(session(), model)), 600);
	}

	private Release lastRelease(Language language) {
		return box().languageManager().lastRelease(language);
	}

}