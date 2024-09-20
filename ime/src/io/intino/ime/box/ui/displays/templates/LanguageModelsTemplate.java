package io.intino.ime.box.ui.displays.templates;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.datasources.LanguageModelsDatasource;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;

public class LanguageModelsTemplate extends AbstractLanguageModelsTemplate<ImeBox> {
	private Language language;

	public LanguageModelsTemplate(ImeBox box) {
		super(box);
	}

	public void language(String name) {
		this.language = box().languageManager().get(name);
	}

	@Override
	public void init() {
		super.init();
		header.onSearch(this::filter);
		modelsCatalog.onOpenModel(this::notifyOpeningModel);
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
	}

	private void notifyOpeningModel(Model model) {
		bodyBlock.hide();
		openingModelMessage.value(String.format(translate("Opening %s"), model.label()));
		openingModelBlock.show();
	}

	private void filter(String condition) {
		modelsCatalog.filter(condition);
	}
}