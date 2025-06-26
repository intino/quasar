package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.AddCollectionItemEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.ui.datasources.CollectionLanguagesDatasource;
import io.quassar.editor.box.ui.datasources.LanguagesDatasource;
import io.quassar.editor.box.ui.displays.items.LanguageCollectionItem;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.model.Collection;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LogoSize;

import java.util.Collections;
import java.util.List;

public class LanguagesCollectionTemplate extends AbstractLanguagesCollectionTemplate<EditorBox> {
	private Collection collection;

	public LanguagesCollectionTemplate(EditorBox box) {
		super(box);
	}

	public void collection(Collection collection) {
		this.collection = collection;
	}

	public void filter(String condition) {
		languagesCollectionCatalog.filter(condition);
	}

	public void filter(String grouping, List<String> values) {
		languagesCollectionCatalog.filter(grouping, values);
	}

	@Override
	public void init() {
		super.init();
		languagesCollectionCatalog.onAddItem(this::refresh);
		searchBox.onEnterPress(e -> filter(e.value()));
		searchBox.onChange(e -> filter(e.value()));
	}

	@Override
	public void refresh() {
		super.refresh();
		refreshLanguages();
	}

	private void refreshLanguages() {
		LanguagesDatasource source = new CollectionLanguagesDatasource(box(), session(), collection);
		languagesCollectionCatalog.source(source);
		noItemsMessage.visible(source.itemCount() == 0);
		searchBox.visible(source.itemCount() > DisplayHelper.MinItemsCount*4);
	}

	private void refresh(AddCollectionItemEvent event) {
		Language language = event.item();
		LanguageCollectionItem item = event.component();
		item.logo.value(LanguageHelper.logo(language, LogoSize.S50, box()));
		item.logoLink.address(path -> PathHelper.languagePath(path, language));
		item.name.title(language.name());
		item.name.address(path -> PathHelper.languagePath(path, language));
	}

}