package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.AddCollectionItemEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.ui.datasources.LanguagesDatasource;
import io.quassar.editor.box.ui.displays.items.LanguageMagazineItem;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.ui.types.LanguagesTab;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.box.util.SessionHelper;
import io.quassar.editor.model.Language;

import java.util.List;

public class LanguagesTemplate extends AbstractLanguagesTemplate<EditorBox> {
	private LanguagesTab tab;

	public LanguagesTemplate(EditorBox box) {
		super(box);
	}

	public void open(String tab) {
		this.tab = tab != null ? LanguagesTab.from(tab) : SessionHelper.languagesTab(session());
		refresh();
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
	}

	@Override
	public void refresh() {
		super.refresh();
		refreshHeader();
		refreshLanguages();
	}

	private void refreshHeader() {
		headerStamp.tab(tab);
		headerStamp.refresh();
	}

	private void refreshLanguages() {
		languagesMagazine.source(new LanguagesDatasource(box(), session(), tab));
	}

	private void refresh(AddCollectionItemEvent event) {
		Language language = event.item();
		LanguageMagazineItem item = event.component();
		item.logo.value(LanguageHelper.logo(language, box()));
		item.languageTitleLink.title(language.name());
		item.languageTitleLink.address(path -> PathHelper.languagePath(path, language));
		item.description.value(language.description());
		item.languageLink.address(path -> PathHelper.languagePath(path, language));
		item.owner.value(language.isFoundational() ? translate("Quassar") : language.owner());
		item.createDate.value(language.createDate());
		item.modelsCount.value(language.modelsCount());
		item.parent.value(!language.isFoundational() ? language.parent() : "-");
		item.viewModels.readonly(language.modelsCount() == 0);
		item.viewModels.address(path -> PathHelper.languagePath(path, language, LanguageTab.PublicModels));
	}

}