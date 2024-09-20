package io.intino.ime.box.ui.displays.templates;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.datasources.LanguagesDatasource;
import io.intino.ime.box.util.Formatters;
import io.intino.ime.model.Model;

public class HomeTemplate extends AbstractHomeTemplate<ImeBox> {
	private LanguagesDatasource.Sorting selectedSorting;

	public HomeTemplate(ImeBox box) {
		super(box);
	}

	@Override
	public void init() {
		super.init();
		redirectIfCallback();
		header.onSearch(this::filter);
		languagesCatalog.onOpenLanguage(this::notifyOpeningModel);
		languagesCatalog.onSelectOwner(this::refreshNavigationToolbar);
		navigationDialogRemove.onExecute(e -> removeOwnerFilter());
		initSortings();
		refresh();
	}

	@Override
	public void refresh() {
		super.refresh();
		header.refresh();
		LanguagesDatasource source = new LanguagesDatasource(box(), session());
		countLanguages.value(Formatters.countMessage(source.itemCount(), "language", "languages", language()));
		refreshLanguages(source);
	}

	private void refreshLanguages(LanguagesDatasource source) {
		languagesCatalog.source(source);
		languagesCatalog.refresh();
	}

	private void notifyOpeningModel(Model model) {
		bodyBlock.hide();
		openingModelMessage.value(String.format(translate("Opening %s"), model.label()));
		searchingModelsBlock.show();
	}

	private void filter(String condition) {
		languagesCatalog.filter(condition);
	}

	private void removeOwnerFilter() {
		languagesCatalog.removeOwnerFilter();
		refreshNavigationToolbar(null);
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

	private void redirectIfCallback() {
		String callback = session().preference("callback");
		if (callback == null) return;
		session().add("callback", null);
		notifier.redirect(callback);
	}

}