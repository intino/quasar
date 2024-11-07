package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.components.Layer;
import io.intino.alexandria.ui.displays.events.SelectionEvent;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.ui.ViewMode;
import io.intino.ime.box.ui.datasources.LanguagesDatasource;
import io.intino.ime.box.ui.datasources.ModelsDatasource;
import io.intino.ime.box.util.Formatters;
import io.intino.ime.box.util.LanguageHelper;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;

import java.util.Set;
import java.util.UUID;

public class DashboardTemplate extends AbstractDashboardTemplate<ImeBox> {

	public DashboardTemplate(ImeBox box) {
		super(box);
	}

	@Override
	public void init() {
		super.init();
		header.onOpenSearch(e -> openSearch());
		header.onChangeView(e -> refresh());
		languageGroupSelector.onSelect(this::selectLanguageGroupOption);
		modelGroupSelector.onSelect(this::selectModelGroupOption);
		languageGroupSelector.onShow(e -> languageGroupSelector.select("allLanguagesOption"));
		modelGroupSelector.onShow(e -> modelGroupSelector.select("allModelsOption"));
		languagesCatalog.onOpenLanguage(this::notifyOpening);
		languagesCatalog.onOpenModel(this::notifyOpening);
		modelsCatalog.onOpenModel(this::notifyOpening);
		openSearchLayerTrigger.onOpen(e -> openSearch(e.layer()));
	}

	@Override
	public void refresh() {
		super.refresh();
		header.view(HeaderTemplate.View.Dashboard);
		header.refresh();
		ViewMode viewMode = DisplayHelper.viewMode(session());
		languageGroupSelector.visible(viewMode == ViewMode.Languages);
		modelGroupSelector.visible(viewMode == ViewMode.Models);
		if (viewMode == ViewMode.Languages) languageGroupSelector.select("allLanguagesOption");
		else modelGroupSelector.select("allModelsOption");
	}

	private void selectLanguageGroupOption(SelectionEvent event) {
		String selected = event.selection().isEmpty() ? "allLanguagesOption" : (String) event.selection().getFirst();
		if (selected.equals("allLanguagesOption")) refreshLanguages("My languages", new LanguagesDatasource(box(), session(), username()));
		else if (selected.equals("publicLanguagesOption")) refreshLanguages("My public languages", new LanguagesDatasource(box(), session(), username(), false));
		else refreshLanguages("My private languages", new LanguagesDatasource(box(), session(), username(), true));
	}

	private void selectModelGroupOption(SelectionEvent event) {
		String selected = event.selection().isEmpty() ? "allModelsOption" : (String) event.selection().getFirst();
		if (selected.equals("publicModelsOption")) refreshModels("My public models", new ModelsDatasource(box(), session(), false));
		else if (selected.equals("privateModelsOption")) refreshModels("My private models", new ModelsDatasource(box(), session(), true));
		else refreshModels("My models", new ModelsDatasource(box(), session(), null));
	}

	private void refreshLanguages(String title, LanguagesDatasource source) {
		showLanguages();
		languagesCatalog.title(title);
		languagesCatalog.source(source);
		languagesCatalog.refresh();
		leftPanelBlock.formats(Set.of("leftPanelStyle", "languagesStyle"));
		mainPanelBlock.formats(Set.of("dashboardMainPanelStyle", "languagesStyle"));
		count.value(Formatters.countMessage(languagesCatalog.itemCount(), "language", "languages", language()));
	}

	private void refreshModels(String title, ModelsDatasource source) {
		showModels();
		modelsCatalog.title(title);
		modelsCatalog.source(source);
		modelsCatalog.refresh();
		leftPanelBlock.formats(Set.of("leftPanelStyle", "modelsStyle"));
		mainPanelBlock.formats(Set.of("dashboardMainPanelStyle", "modelsStyle"));
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

	private void notifyOpening(Language language) {
		bodyBlock.hide();
		openingMessage.value(String.format(translate("Opening %s"), LanguageHelper.label(language, this::translate)));
		openingBlock.show();
	}

	private void notifyOpening(Model model) {
		bodyBlock.hide();
		openingMessage.value(String.format(translate("Opening %s"), ModelHelper.label(model, language(), box())));
		openingBlock.show();
	}

	private void openSearch() {
		openSearchLayerTrigger.address(path -> PathHelper.searchPath());
		openSearchLayerTrigger.launch();
	}

	private void openSearch(Layer<?, ?> layer) {
		HomeTemplate template = new HomeTemplate(box());
		template.id(UUID.randomUUID().toString());
		layer.template(template);
		template.page(HomeTemplate.Page.Search);
		template.refresh();
	}

}