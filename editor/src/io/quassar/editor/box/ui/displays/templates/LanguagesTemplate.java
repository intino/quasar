package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.AddCollectionItemEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.ui.datasources.LanguagesDatasource;
import io.quassar.editor.box.ui.displays.items.LanguageItem;
import io.quassar.editor.box.ui.types.LanguagesTab;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.box.util.SessionHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LogoSize;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class LanguagesTemplate extends AbstractLanguagesTemplate<EditorBox> {
	private LanguagesTab tab;
	private Consumer<Language> selectListener;
	private LanguagesDatasource source;

	public LanguagesTemplate(EditorBox box) {
		super(box);
	}

	public void open(String tab) {
		this.tab = tab != null ? LanguagesTab.from(tab) : SessionHelper.languagesTab(session());
		refresh();
	}

	public void filter(String condition) {
		languagesCatalog.filter(condition);
	}

	public void filter(String grouping, List<String> values) {
		languagesCatalog.filter(grouping, values);
	}

	public void onSelect(Consumer<Language> listener) {
		this.selectListener = listener;
	}

	public void source(LanguagesDatasource source) {
		this.source = source;
	}

	@Override
	public void init() {
		super.init();
		languagesCatalog.onAddItem(this::refresh);
		searchBox.onEnterPress(e -> filter(e.value()));
		searchBox.onChange(e -> filter(e.value()));
	}

	@Override
	public void refresh() {
		super.refresh();
		searchBox.value(null);
		refreshLanguages();
	}

	private void refreshLanguages() {
		LanguagesDatasource source = this.source != null ? this.source : new LanguagesDatasource(box(), session());
		languagesCatalog.source(source);
		searchBox.visible(source.itemCount(null, Collections.emptyList()) > DisplayHelper.MinItemsCount);
	}

	private void refresh(AddCollectionItemEvent event) {
		Language language = event.item();
		LanguageItem item = event.component();
		item.logo.value(LanguageHelper.logo(language, LogoSize.S100, box()));
		refreshLogo(language, item);
		refreshLogoSelector(language, item);
		refreshName(language, item);
		refreshNameSelector(language, item);
		item.title.value(language.title());
		item.description.value(language.description());
	}

	private void refreshLogo(Language language, LanguageItem item) {
		item.logoLink.visible(selectListener == null);
		if (!item.logoLink.isVisible()) return;
		item.logoLink.address(path -> PathHelper.languagePath(path, language));
	}

	private void refreshName(Language language, LanguageItem item) {
		item.name.visible(selectListener == null);
		if (!item.name.isVisible()) return;
		item.name.title(LanguageHelper.label(language, this::translate));
		item.name.address(path -> PathHelper.languagePath(path, language));
	}

	private void refreshLogoSelector(Language language, LanguageItem item) {
		item.logoSelectorLink.visible(selectListener != null);
		if (!item.logoSelectorLink.isVisible()) return;
		item.logoSelectorLink.onExecute(e -> notifySelect(language));
	}

	private void refreshNameSelector(Language language, LanguageItem item) {
		item.nameSelector.visible(selectListener != null);
		if (!item.nameSelector.isVisible()) return;
		item.nameSelector.title(LanguageHelper.label(language, this::translate));
		item.nameSelector.onExecute(e -> notifySelect(language));
	}

	private void notifySelect(Language language) {
		selectListener.accept(language);
	}

}