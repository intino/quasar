package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.AddCollectionItemEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.ui.datasources.LanguagesDatasource;
import io.quassar.editor.box.ui.displays.items.LanguageItem;
import io.quassar.editor.box.ui.displays.items.LanguageLandingItem;
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

public class LanguagesLandingTemplate extends AbstractLanguagesLandingTemplate<EditorBox> {
	private LanguagesTab tab;
	private Consumer<Language> selectListener;

	public LanguagesLandingTemplate(EditorBox box) {
		super(box);
	}

	public void open(String tab) {
		this.tab = tab != null ? LanguagesTab.from(tab) : SessionHelper.languagesTab(session());
		refresh();
	}

	public void filter(String condition) {
		languagesLandingCatalog.filter(condition);
	}

	public void filter(String grouping, List<String> values) {
		languagesLandingCatalog.filter(grouping, values);
	}

	public void onSelect(Consumer<Language> listener) {
		this.selectListener = listener;
	}

	@Override
	public void init() {
		super.init();
		languagesLandingCatalog.onAddItem(this::refresh);
		searchBox.onEnterPress(e -> filter(e.value()));
		searchBox.onChange(e -> filter(e.value()));
	}

	@Override
	public void refresh() {
		super.refresh();
		refreshLanguages();
	}

	private void refreshLanguages() {
		LanguagesDatasource source = new LanguagesDatasource(box(), session());
		languagesLandingCatalog.source(source);
		searchBox.visible(source.itemCount(null, Collections.emptyList()) > DisplayHelper.MinItemsCount);
	}

	private void refresh(AddCollectionItemEvent event) {
		Language language = event.item();
		LanguageLandingItem item = event.component();
		item.logo.value(LanguageHelper.logo(language, LogoSize.S100, box()));
		refreshLogo(language, item);
		refreshLogoSelector(language, item);
		refreshName(language, item);
		refreshNameSelector(language, item);
		item.title.value(language.title());
		item.description.value(language.description());
	}

	private void refreshLogo(Language language, LanguageLandingItem item) {
		item.logoLink.visible(selectListener == null);
		if (!item.logoLink.isVisible()) return;
		item.logoLink.address(path -> PathHelper.languagePath(path, language));
	}

	private void refreshName(Language language, LanguageLandingItem item) {
		item.name.visible(selectListener == null);
		if (!item.name.isVisible()) return;
		item.name.title(LanguageHelper.label(language, this::translate));
		item.name.address(path -> PathHelper.languagePath(path, language));
	}

	private void refreshLogoSelector(Language language, LanguageLandingItem item) {
		item.logoSelectorLink.visible(selectListener != null);
		if (!item.logoSelectorLink.isVisible()) return;
		item.logoSelectorLink.onExecute(e -> notifySelect(language));
	}

	private void refreshNameSelector(Language language, LanguageLandingItem item) {
		item.nameSelector.visible(selectListener != null);
		if (!item.nameSelector.isVisible()) return;
		item.nameSelector.title(LanguageHelper.label(language, this::translate));
		item.nameSelector.onExecute(e -> notifySelect(language));
	}

	private void notifySelect(Language language) {
		selectListener.accept(language);
	}

}