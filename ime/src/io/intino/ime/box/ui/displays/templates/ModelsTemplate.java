package io.intino.ime.box.ui.displays.templates;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.ui.datasources.LanguageModelsDatasource;
import io.intino.ime.box.util.Formatters;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;

import java.util.Map;

public class ModelsTemplate extends AbstractModelsTemplate<ImeBox> {
	private Language language;

	public ModelsTemplate(ImeBox box) {
		super(box);
	}

	public void filters(String filters) {
		Map<String, String> filtersMap = PathHelper.filtersFrom(filters);
		this.language = filtersMap.containsKey("language") ? box().languageManager().get(filtersMap.get("language")) : null;
	}

	@Override
	public void init() {
		super.init();
		header.onSearch(this::filter);
		modelsCatalog.onOpenModel(this::notifyOpeningModel);
		navigationDialogRemove.onExecute(e -> removeParentFilter());
		initFilters();
	}

	@Override
	public void refresh() {
		super.refresh();
		header.language(language);
		header.refresh();
		modelsCatalog.source(new LanguageModelsDatasource(box(), session(), language));
		modelsCatalog.language(language);
		modelsCatalog.readonly(true);
		modelsCatalog.refresh();
		refreshNavigationToolbar();
	}

	private void notifyOpeningModel(Model model) {
		bodyBlock.hide();
		openingModelMessage.value(String.format(translate("Opening %s"), ModelHelper.label(model, language(), box())));
		openingModelBlock.show();
	}

	private void filter(String condition) {
		modelsCatalog.filter(condition);
	}

	private void removeParentFilter() {
		notifier.redirect(PathHelper.modelsUrl(session()));
	}

	private void refreshNavigationToolbar() {
		navigationDialog.visible(language != null);
		if (navigationDialog.visible()) navigationDialogTitle.value(String.format(translate("Defined with %s"), language.name()));
		countLanguages.value(Formatters.countMessage(modelsCatalog.itemCount(), "models", "models", language()));
	}

	private void initFilters() {
		modelsCatalog.bindTo(owner);
	}

}