package io.intino.ime.box.ui.displays.templates;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.ui.datasources.LanguagesDatasource;
import io.intino.ime.box.ui.datasources.ParentLanguagesDatasource;
import io.intino.ime.box.util.Formatters;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;

import java.util.Map;

import static java.util.Collections.emptyList;

public class LanguagesTemplate extends AbstractLanguagesTemplate<ImeBox> {
	private Language parent;
	private LanguagesDatasource.Sorting selectedSorting;

	public LanguagesTemplate(ImeBox box) {
		super(box);
	}

	public void filters(String filters) {
		Map<String, String> filtersMap = PathHelper.filtersFrom(filters);
		this.parent = filtersMap.containsKey("parent") ? box().languageManager().get(filtersMap.get("parent")) : null;
	}

	@Override
	public void init() {
		super.init();
		redirectIfCallback();
		header.language(parent);
		header.onSearch(this::filter);
		languagesCatalog.onOpenLanguage(this::notifyOpeningModel);
		languagesCatalog.onFilter(this::refreshNavigationToolbar);
		navigationDialogRemove.onExecute(e -> removeParentFilter());
		initFilters();
		initSortings();
		refresh();
	}

	@Override
	public void refresh() {
		super.refresh();
		header.refresh();
		ParentLanguagesDatasource source = new ParentLanguagesDatasource(box(), session(), parent);
		countLanguages.value(Formatters.countMessage(source.itemCount(), "language", "languages", language()));
		refreshLanguages(source);
		refreshNavigationToolbar();
	}

	private void refreshLanguages(LanguagesDatasource source) {
		languagesCatalog.source(source);
		languagesCatalog.refresh();
	}

	private void notifyOpeningModel(Model model) {
		bodyBlock.hide();
		openingModelMessage.value(String.format(translate("Opening %s"), ModelHelper.label(model, language(), box())));
		searchingModelsBlock.show();
	}

	private void filter(String condition) {
		languagesCatalog.filter(condition);
	}

	private void refreshNavigationToolbar(Long count) {
		countLanguages.value(Formatters.countMessage(count, "language", "languages", language()));
	}

	private void initFilters() {
		languagesCatalog.bindTo(owner);
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

	private void removeParentFilter() {
		notifier.redirect(PathHelper.languagesUrl(session()));
	}

	private void refreshNavigationToolbar() {
		navigationDialog.visible(parent != null);
		if (navigationDialog.visible()) navigationDialogTitle.value(String.format(translate("Defined with %s"), parent.name()));
		countLanguages.value(Formatters.countMessage(languagesCatalog.itemCount(), "language", "languages", language()));
	}

}