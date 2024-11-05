package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.AddCollectionItemEvent;
import io.intino.alexandria.ui.displays.events.AddItemEvent;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.datasources.LanguagesDatasource;
import io.intino.ime.box.ui.displays.rows.LanguagesCatalogCollectionRow;
import io.intino.ime.box.ui.model.Owner;
import io.intino.ime.box.ui.model.SearchItem;
import io.intino.ime.box.ui.model.Tag;
import io.intino.ime.box.util.LanguageHelper;
import io.intino.ime.model.Language;

import java.util.List;
import java.util.function.Consumer;

public class LanguagesCatalogTable extends AbstractLanguagesCatalogTable<ImeBox> {
	private String _title = null;
	private LanguagesDatasource source;
	private Consumer<Language> openLanguageListener;

	public LanguagesCatalogTable(ImeBox box) {
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

	public void sort(LanguagesDatasource.Sorting sorting) {
		source.sort(sorting);
		languagesCatalogCollection.reload();
	}

	public long itemCount() {
		return languagesCatalogCollection.itemCount();
	}

	public void filter(SearchItem item) {
		LanguagesDatasource source = languagesCatalogCollection.source();
		source.tag(item instanceof Tag ? item.name() : null);
		source.owner(item instanceof Owner ? item.name() : null);
		languagesCatalogCollection.reload();
	}

	public void filter(String condition) {
		languagesCatalogCollection.filter(condition);
	}

	public void filter(String grouping, List<String> values) {
		languagesCatalogCollection.filter(grouping, values);
	}

	@Override
	public void init() {
		super.init();
		languagesCatalogCollection.onAddItem(this::refresh);
		searchBox.onChange(e -> filter((String) e.value()));
		searchBox.onEnterPress(e -> filter((String) e.value()));
	}

	@Override
	public void refresh() {
		super.refresh();
		title.visible(_title != null);
		if (title.isVisible()) title.value(_title);
		languagesCatalogCollection.source(source);
	}

	private void refresh(AddCollectionItemEvent event) {
		Language language = event.item();
		LanguagesCatalogCollectionRow item = event.component();
		item.lccLogoItem.logo.value(LanguageHelper.logo(language, box()));
		item.lccNameItem.name.title(language.name());
		item.lccNameItem.name.onExecute(e -> openLanguageListener.accept(language));
		item.lccDescriptionItem.description.value(language.description());
		item.lccOwnerItem.owner.value(language.owner());
	}

}