package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.SelectionEvent;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.datasources.LanguagesDatasource;
import io.intino.ime.box.ui.datasources.ModelsDatasource;
import io.intino.ime.model.Model;

public class UserHomeTemplate extends AbstractUserHomeTemplate<ImeBox> {

	public UserHomeTemplate(ImeBox box) {
		super(box);
	}

	@Override
	public void init() {
		super.init();
		groupSelector.onSelect(this::selectGroupSelectorOption);
		groupSelector.select("allLanguagesOption");
		languagesCatalog.onOpenLanguage(this::notifyOpeningModel);
		modelsCatalog.onOpenModel(this::notifyOpeningModel);
	}

	private void selectGroupSelectorOption(SelectionEvent event) {
		String selected = event.selection().isEmpty() ? "allLanguagesOption" : (String) event.selection().getFirst();
		if (selected.equals("allLanguagesOption")) refreshLanguages("All languages", new LanguagesDatasource(box(), session(), username()));
		else if (selected.equals("publicLanguagesOption")) refreshLanguages("Public languages", new LanguagesDatasource(box(), session(), username(), false));
		else if (selected.equals("privateLanguagesOption")) refreshLanguages("Private languages", new LanguagesDatasource(box(), session(), username(), true));
		else if (selected.equals("publicModelsOption")) refreshModels("Public models", new ModelsDatasource(box(), session(), false));
		else if (selected.equals("privateModelsOption")) refreshModels("Private models", new ModelsDatasource(box(), session(), true));
		else refreshModels("All models", new ModelsDatasource(box(), session(), null));
	}

	private void refreshLanguages(String title, LanguagesDatasource source) {
		showLanguages();
		languagesCatalog.title(title);
		languagesCatalog.source(source);
		languagesCatalog.refresh();
	}

	private void refreshModels(String title, ModelsDatasource source) {
		showModels();
		modelsCatalog.title(title);
		modelsCatalog.source(source);
		modelsCatalog.refresh();
	}

	private void showLanguages() {
		modelsCatalogBlock.hide();
		languagesCatalogBlock.show();
	}

	private void showModels() {
		modelsCatalogBlock.show();
		languagesCatalogBlock.hide();
	}

	private void notifyOpeningModel(Model model) {
		bodyBlock.hide();
		openingModelMessage.value(String.format(translate("Opening %s"), model.label()));
		openingModelBlock.show();
	}

}