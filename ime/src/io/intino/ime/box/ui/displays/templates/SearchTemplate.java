package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.SelectionEvent;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.datasources.LanguagesDatasource;
import io.intino.ime.box.ui.datasources.ModelsDatasource;
import io.intino.ime.box.ui.datasources.OwnersDatasource;
import io.intino.ime.box.ui.datasources.TagsDatasource;
import io.intino.ime.box.ui.model.Owner;
import io.intino.ime.box.ui.model.Tag;
import io.intino.ime.model.Language;
import io.intino.ime.box.ui.model.SearchItem;

import java.util.List;

public class SearchTemplate extends AbstractSearchTemplate<ImeBox> {
	private String condition;
	private SearchItem selectedItem;

	public SearchTemplate(ImeBox box) {
		super(box);
	}

	public void condition(String condition) {
		this.condition = condition;
	}

	@Override
	public void init() {
		super.init();
		header.onSearch(this::filter);
		mainBlock.onShow(e -> refreshMain());
		searchItemBlock.onInit(e -> initSearchItem());
		searchItemBlock.onShow(e -> refreshSearchItem());
		searchResultsBlock.onInit(e -> initSearchResults());
		searchResultsBlock.onShow(e -> refreshSearchResults());
	}

	private void refreshMain() {
		refreshCategories();
		refreshOwners();
		refreshLanguages();
	}

	@Override
	public void refresh() {
		super.refresh();
		header.condition(condition);
		header.refresh();
		if (condition != null && !condition.isEmpty()) filter(condition);
		else showMain();
	}

	private void initSearchItem() {
		itemsBack.onExecute(e -> showMain());
		itemLanguagesCatalog.source(new LanguagesDatasource(box(), session()));
		itemLanguagesCatalog.refresh();
	}

	private void refreshSearchItem() {
		itemLanguageTitle.value(selectedItem.name());
		itemLanguagesCatalog.filter(selectedItem);
	}

	private void initSearchResults() {
		searchResultsBlock.searchLanguagesBlock.onInit(e -> initSearchLanguages());
		searchResultsBlock.searchLanguagesBlock.onShow(e -> refreshSearchLanguages());
		searchResultsBlock.searchModelsBlock.onInit(e -> initSearchModels());
		searchResultsBlock.searchModelsBlock.onShow(e -> refreshSearchModels());
		searchResultsSelector.onSelect(this::selectSearchView);
		searchResultsSelector.select("searchLanguagesOption");
	}

	private void selectSearchView(SelectionEvent event) {
		List<String> selection = event.selection();
		searchResultsBlock.searchLanguagesBlock.hide();
		searchResultsBlock.searchModelsBlock.hide();
		if (selection.isEmpty() || selection.getFirst().equals("searchLanguagesOption")) searchResultsBlock.searchLanguagesBlock.show();
		else searchResultsBlock.searchModelsBlock.show();
	}

	private void refreshSearchResults() {
		refreshSearchLanguages();
		refreshSearchModels();
	}

	private void initSearchLanguages() {
		searchResultsBlock.searchLanguagesBlock.languagesCatalog.source(new LanguagesDatasource(box(), session()));
		searchResultsBlock.searchLanguagesBlock.languagesCatalog.refresh();
	}

	private void refreshSearchLanguages() {
		if (!searchResultsBlock.searchLanguagesBlock.isVisible()) return;
		searchResultsBlock.searchLanguagesBlock.languagesCatalog.title("");
		searchResultsBlock.searchLanguagesBlock.languagesCatalog.filter(condition);
	}

	private void initSearchModels() {
		searchResultsBlock.searchModelsBlock.modelsCatalog.source(new ModelsDatasource(box(), session(), null));
		searchResultsBlock.searchModelsBlock.modelsCatalog.refresh();
	}

	private void refreshSearchModels() {
		if (!searchResultsBlock.searchModelsBlock.isVisible()) return;
		searchResultsBlock.searchModelsBlock.modelsCatalog.title("");
		searchResultsBlock.searchModelsBlock.modelsCatalog.filter(condition);
	}

	private void filter(String condition) {
		this.condition = condition;
		showSearchResults();
		refreshSearchResults();
	}

	private void showItemBlock() {
		searchResultsBlock.hide();
		mainBlock.hide();
		searchItemBlock.show();
	}

	private void showMain() {
		searchResultsBlock.hide();
		mainBlock.show();
		searchItemBlock.hide();
	}

	private void showSearchResults() {
		searchResultsBlock.show();
		mainBlock.hide();
		searchItemBlock.hide();
	}

	private void refreshCategories() {
		TagsDatasource source = new TagsDatasource(box(), session());
		List<Tag> items = source.items(0, 6, null, List.of(), List.of());
		mainBlock.categoriesBlock.hide();
		tags.clear();
		items.forEach(i -> fill(i, tags.add()));
		mainBlock.categoriesBlock.show();
	}

	private void refreshOwners() {
		OwnersDatasource source = new OwnersDatasource(box(), session());
		List<Owner> items = source.items(0, 6, null, List.of(), List.of());
		mainBlock.ownersBlock.hide();
		owners.clear();
		items.forEach(i -> fill(i, owners.add()));
		mainBlock.ownersBlock.show();
	}

	private void fill(SearchItem searchItem, SearchItemTemplate display) {
		display.item(searchItem);
		display.onSelect(e -> open(searchItem));
		display.refresh();
	}

	private void open(SearchItem searchItem) {
		this.selectedItem = searchItem;
		showItemBlock();
	}

	private void refreshLanguages() {
		LanguagesDatasource source = new LanguagesDatasource(box(), session());
		source.sort(LanguagesDatasource.Sorting.MostUsed);
		List<Language> items = source.items(0, 5, null, List.of(), List.of());
		refreshMostVisitedLanguages(items);
		source.sort(LanguagesDatasource.Sorting.MostRecent);
		items = source.items(0, 5, null, List.of(), List.of(LanguagesDatasource.Sorting.MostRecent.name()));
		refreshRecentLanguages(items);
	}

	private void refreshMostVisitedLanguages(List<Language> items) {
		mainBlock.mostVisitedLanguagesBlock.hide();
		mostVisitedLanguages.clear();
		items.forEach(i -> fill(i, mostVisitedLanguages.add()));
		mainBlock.mostVisitedLanguagesBlock.show();
	}

	private void refreshRecentLanguages(List<Language> items) {
		mainBlock.recentLanguagesBlock.hide();
		recentLanguages.clear();
		items.forEach(i -> fill(i, recentLanguages.add()));
		mainBlock.recentLanguagesBlock.show();
	}

	private void fill(Language language, LanguageItemTemplate display) {
		display.language(language);
		display.refresh();
	}

}