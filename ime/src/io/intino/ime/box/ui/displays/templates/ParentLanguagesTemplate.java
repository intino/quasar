package io.intino.ime.box.ui.displays.templates;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.datasources.LanguagesDatasource;
import io.intino.ime.box.ui.datasources.ParentLanguagesDatasource;
import io.intino.ime.box.util.Formatters;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;

public class ParentLanguagesTemplate extends AbstractParentLanguagesTemplate<ImeBox> {
	private Language language;
	private LanguagesDatasource.Sorting selectedSorting;

	public ParentLanguagesTemplate(ImeBox box) {
		super(box);
	}

	public void language(String name) {
		this.language = box().languageManager().get(name);
	}

	@Override
	public void init() {
		super.init();
		header.onSearch(this::filter);
		languagesCatalog.onOpenLanguage(this::notifyOpeningLanguage);
		languagesCatalog.onSelectOwner(this::refreshNavigationToolbar);
		navigationDialogRemove.onExecute(e -> removeOwnerFilter());
		initSortings();
	}

	@Override
	public void refresh() {
		super.refresh();
		header.language(language);
		header.refresh();
		ParentLanguagesDatasource source = new ParentLanguagesDatasource(box(), session(), language);
		countLanguages.value(Formatters.countMessage(source.itemCount(), "language", "languages", language()));
		refreshLanguages(source);
	}

	private void refreshLanguages(ParentLanguagesDatasource source) {
		languagesCatalog.source(source);
		languagesCatalog.refresh();
	}

	private void filter(String condition) {
		languagesCatalog.filter(condition);
	}

	private void refreshNavigationToolbar(String owner) {
		navigationDialogTitle.value(owner);
		navigationDialog.visible(owner != null);
		countLanguages.value(Formatters.countMessage(languagesCatalog.itemCount(), "language", "languages", language()));
	}

	private void initSortings() {
		mostUsedLink.onExecute(e -> sortBy(LanguagesDatasource.Sorting.MostUsed));
		mostRecentsLink.onExecute(e -> sortBy(LanguagesDatasource.Sorting.MostRecent));
	}

	private void notifyOpeningLanguage(Model model) {
		bodyBlock.hide();
		openingLanguageMessage.value(String.format(translate("Opening %s"), model.label()));
	}

	private void removeOwnerFilter() {
		languagesCatalog.removeOwnerFilter();
		refreshNavigationToolbar(null);
	}

	private void sortBy(LanguagesDatasource.Sorting sorting) {
		this.selectedSorting = sorting;
		languagesCatalog.sort(sorting);
		refreshSortings();
	}

	private void refreshSortings() {
		mostUsedLink.visible(selectedSorting != LanguagesDatasource.Sorting.MostUsed);
		mostUsedText.visible(selectedSorting == LanguagesDatasource.Sorting.MostUsed);
		mostRecentsLink.visible(selectedSorting != LanguagesDatasource.Sorting.MostRecent);
		mostRecentsText.visible(selectedSorting == LanguagesDatasource.Sorting.MostRecent);
	}

}