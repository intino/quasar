package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.Resource;
import io.intino.alexandria.ui.displays.components.Grouping;
import io.intino.alexandria.ui.displays.events.AddItemEvent;
import io.intino.alexandria.ui.displays.notifiers.GroupingNotifier;
import io.intino.alexandria.ui.services.push.User;
import io.intino.alexandria.ui.utils.DelayerUtil;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.LanguageCommands;
import io.intino.ime.box.commands.ModelCommands;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.ui.datasources.DatasourceHelper;
import io.intino.ime.box.ui.datasources.LanguagesDatasource;
import io.intino.ime.box.ui.displays.items.LanguageMagazineItem;
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
	private Consumer<Model> openModelListener;
	private Consumer<Long> filterListener;
	private Language selectedLanguage;
	private Release selectedRelease;

	public LanguagesCatalog(ImeBox box) {
		super(box);
	}

	public void title(String title) {
		this._title = title;
	}

	public void source(LanguagesDatasource source) {
		this.source = source;
	}

	public void onOpenLanguage(Consumer<Model> listener) {
		this.openModelListener = listener;
	}

	public void onFilter(Consumer<Long> listener) {
		this.filterListener = listener;
	}

	public void bindTo(Grouping<GroupingNotifier, ImeBox> grouping) {
		grouping.bindTo(languagesMagazine);
	}

	public void sort(LanguagesDatasource.Sorting sorting) {
		source.sort(sorting);
		languagesMagazine.reload();
	}

	public long itemCount() {
		return languagesMagazine.itemCount();
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
		addPrivateModelDialog.onOpen(e -> refreshAddPrivateModelDialog());
		createModel.onExecute(e -> createModel());
		releasesDialog.onOpen(e -> refreshReleasesDialog());
		releasesCatalog.onCreateLanguage(e -> createLanguage(e, user()));
		releasesCatalog.onCreateModel(e -> createModel(e, user()));
		labelField.onEnterPress(e -> createModel());
		createLanguageDialog.onOpen(e -> refreshCreateLanguageDialog());
		createLanguage.onExecute(e -> createLanguage());
		languagesMagazine.onRefreshItemCount(e -> filterListener.accept(e.count()));
	}

	@Override
	public void refresh() {
		super.refresh();
		title.visible(_title != null);
		if (title.isVisible()) title.value(translate(_title));
		languagesMagazine.source(source);
	}

	private void refresh(AddItemEvent event) {
		Language language = event.item();
		Release release = box().languageManager().lastRelease(language);
		LanguageMagazineItem item = event.component();
		item.logo.value(LanguageHelper.logo(language, box()));
		item.languageTitleLink.title(language.name());
		item.languageTitleLink.path(PathHelper.languagePath(language));
		item.description.value(language.description());
		item.languageLink.path(PathHelper.languagePath(language));
		item.owner.title(language.owner());
		item.owner.onExecute(e -> filterOwner(language.owner()));
		item.privatePill.visible(language.isPrivate());
		item.createDate.value(language.createDate());
		item.modelsCount.value(language.modelsCount());
		item.parent.value(!language.isFoundational() ? language.parent() : "-");
		item.createLanguageTrigger.visible(LanguageHelper.canCreateLanguage(language, release, user()));
		item.createLanguageTrigger.bindTo(createLanguageDialog);
		item.createLanguageTrigger.onOpen(e -> refreshCreateLanguageDialog(language));
		item.addModel.onExecute(e -> createModel(lastRelease(language)));
		item.addModel.visible(ModelHelper.canAddModel(release) && user() == null);
		item.addPrivateModel.bindTo(addPrivateModelDialog);
		item.addPrivateModel.onOpen(e -> refreshAddPrivateModelDialog(language));
		item.addPrivateModel.visible(ModelHelper.canAddModel(release) && user() != null);
		item.releasesDialogTrigger.bindTo(releasesDialog);
		item.releasesDialogTrigger.onOpen(e -> refreshReleasesDialog(language));
	}

	private void filterOwner(String owner) {
		languagesMagazine.filter(DatasourceHelper.Owner, List.of(owner));
	}

	private void refreshAddPrivateModelDialog(Language language) {
		this.selectedLanguage = language;
		this.selectedRelease = lastRelease(selectedLanguage);
		refreshAddPrivateModelDialog();
	}

	private void refreshAddPrivateModelDialog() {
		if (selectedLanguage == null || selectedRelease == null) return;
		languageField.value(selectedRelease.id());
		labelField.value(null);
	}

	private void refreshReleasesDialog(Language language) {
		this.selectedLanguage = language;
		refreshReleasesDialog();
	}

	private void refreshReleasesDialog() {
		if (selectedLanguage == null) return;
		releasesDialog.title(String.format(translate("%s releases"), selectedLanguage.name()));
		releasesCatalog.language(selectedLanguage);
		releasesCatalog.refresh();
	}

	private void createModel() {
		if (!DisplayHelper.check(labelField, this::translate)) return;
		addPrivateModelDialog.close();
		createModel(selectedRelease, ModelHelper.proposeName(), labelField.value());
	}

	private void createLanguage(Release release, User user) {
		releasesDialog.close();
		if (user == null) return;
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
		releasesDialog.close();
		if (user == null) createModel(release);
		else {
			selectedRelease = release;
			addPrivateModelDialog.open();
		}
	}

	private void createModel(Release release) {
		createModel(release, ModelHelper.proposeName(), translate("(no name)"));
	}

	private void createModel(Release release, String name, String title) {
		open(box().commands(ModelCommands.class).create(name, title, release, DisplayHelper.user(session()), username()));
	}

	private void openModelOf(Language language) {
		open(box().modelManager().modelWith(language));
	}

	private void openModelOf(Release release) {
		open(box().modelManager().modelWith(release.language()));
	}

	private void open(Model model) {
		openModelListener.accept(model);
		DelayerUtil.execute(this, v -> notifier.redirect(PathHelper.modelUrl(session(), model)), 600);
	}

	private Release lastRelease(Language language) {
		return box().languageManager().lastRelease(language);
	}

	private void refreshCreateLanguageDialog(Language language) {
		this.selectedLanguage = language;
		this.selectedRelease = box().languageManager().lastRelease(selectedLanguage);
		refreshCreateLanguageDialog();
	}

	private void refreshCreateLanguageDialog() {
		if (this.selectedLanguage == null) return;
		languageEditor.onAccept(e -> createLanguage(box().languageManager().lastRelease(selectedLanguage)));
		languageEditor.parent(selectedLanguage);
		languageEditor.reset();
	}

}