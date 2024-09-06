package io.intino.ime.box.ui.displays.templates;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.datasources.LanguagesDatasource;
import io.intino.ime.box.util.Formatters;
import io.intino.ime.model.Model;

public class HomeTemplate extends AbstractHomeTemplate<ImeBox> {

	public HomeTemplate(ImeBox box) {
		super(box);
	}

	@Override
	public void init() {
		super.init();
		header.onSearch(this::filter);
		languagesCatalog.onOpenModel(this::notifyOpeningModel);
		refresh();
	}

	@Override
	public void refresh() {
		super.refresh();
		header.refresh();
		LanguagesDatasource source = new LanguagesDatasource(box(), session());
		countLanguages.value(Formatters.countMessage(source.itemCount(), "language", "languages", language()));
		refreshModels(source);
	}

	private void refreshModels(LanguagesDatasource source) {
		languagesCatalog.source(source);
		languagesCatalog.refresh();
	}

	private void notifyOpeningModel(Model model) {
		bodyBlock.hide();
		openingModelMessage.value(String.format(translate("Opening %s"), model.title()));
		searchingModelsBlock.show();
	}

	private void filter(String condition) {
		languagesCatalog.filter(condition);
	}

}