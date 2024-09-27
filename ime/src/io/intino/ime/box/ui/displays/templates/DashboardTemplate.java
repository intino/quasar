package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.SelectionEvent;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.box.ui.ViewMode;
import io.intino.ime.box.ui.datasources.LanguagesDatasource;
import io.intino.ime.box.ui.datasources.ModelsDatasource;
import io.intino.ime.box.util.Formatters;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.model.Model;

import java.util.List;
import java.util.Set;

public class DashboardTemplate extends AbstractDashboardTemplate<ImeBox> {

	public DashboardTemplate(ImeBox box) {
		super(box);
	}

	@Override
	public void init() {
		super.init();
		header.onChangeView(e -> refresh());
		languageGroupSelector.onSelect(this::selectLanguageGroupOption);
		modelGroupSelector.onSelect(this::selectModelGroupOption);
		languageGroupSelector.onShow(e -> languageGroupSelector.select("allLanguagesOption"));
		modelGroupSelector.onShow(e -> modelGroupSelector.select("allModelsOption"));
		languagesCatalog.onOpenLanguage(this::notifyOpeningModel);
		modelsCatalog.onOpenModel(this::notifyOpeningModel);
	}

	@Override
	public void refresh() {
		super.refresh();
		header.showDashboardButton(false);
		header.refresh();
		ViewMode viewMode = DisplayHelper.viewMode(session());
		languageGroupSelector.visible(viewMode == ViewMode.Languages);
		modelGroupSelector.visible(viewMode == ViewMode.Models);
		if (viewMode == ViewMode.Languages) languageGroupSelector.select("allLanguagesOption");
		else modelGroupSelector.select("allModelsOption");
	}

	private void selectLanguageGroupOption(SelectionEvent event) {
		String selected = event.selection().isEmpty() ? "allLanguagesOption" : (String) event.selection().getFirst();
		if (selected.equals("allLanguagesOption")) refreshLanguages("All languages", new LanguagesDatasource(box(), session(), username()));
		else if (selected.equals("publicLanguagesOption")) refreshLanguages("Public languages", new LanguagesDatasource(box(), session(), username(), false));
		else refreshLanguages("Private languages", new LanguagesDatasource(box(), session(), username(), true));
	}

	private void selectModelGroupOption(SelectionEvent event) {
		String selected = event.selection().isEmpty() ? "allModelsOption" : (String) event.selection().getFirst();
		if (selected.equals("publicModelsOption")) refreshModels("Public models", new ModelsDatasource(box(), session(), false));
		else if (selected.equals("privateModelsOption")) refreshModels("Private models", new ModelsDatasource(box(), session(), true));
		else refreshModels("All models", new ModelsDatasource(box(), session(), null));
	}

	private void refreshLanguages(String title, LanguagesDatasource source) {
		showLanguages();
		languagesCatalog.title(title);
		languagesCatalog.source(source);
		languagesCatalog.refresh();
		leftPanelBlock.formats(Set.of("leftPanelStyle", "languagesStyle"));
		mainPanelBlock.formats(Set.of("mainPanelStyle", "languagesStyle"));
		count.value(Formatters.countMessage(languagesCatalog.itemCount(), "language", "languages", language()));
	}

	private void refreshModels(String title, ModelsDatasource source) {
		showModels();
		modelsCatalog.title(title);
		modelsCatalog.source(source);
		modelsCatalog.refresh();
		leftPanelBlock.formats(Set.of("leftPanelStyle", "modelsStyle"));
		mainPanelBlock.formats(Set.of("mainPanelStyle", "modelsStyle"));
		count.value(Formatters.countMessage(modelsCatalog.itemCount(), "model", "models", language()));
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
		openingModelMessage.value(String.format(translate("Opening %s"), ModelHelper.label(model, language(), box())));
		openingModelBlock.show();
	}

}