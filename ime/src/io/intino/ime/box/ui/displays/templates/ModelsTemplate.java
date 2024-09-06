package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.displays.events.AddItemEvent;
import io.intino.alexandria.ui.displays.events.SelectionEvent;
import io.intino.alexandria.ui.utils.DelayerUtil;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.ModelCommands;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.ui.datasources.LanguagesDatasource;
import io.intino.ime.box.ui.datasources.ModelsDatasource;
import io.intino.ime.box.ui.displays.rows.LanguageTableRow;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;

public class ModelsTemplate extends AbstractModelsTemplate<ImeBox> {

	public ModelsTemplate(ImeBox box) {
		super(box);
	}

	@Override
	public void init() {
		super.init();
		initAddModelDialog();
		modelsGroupSelector.onSelect(this::selectModelsGroup);
		modelsGroupSelector.select("allModelsOption");
		modelsCatalog.onOpenModel(this::notifyOpeningModel);
	}

	private void initAddModelDialog() {
		addModelDialog.onOpen(e -> refreshAddModelDialog());
		nameField.onChange(e -> DisplayHelper.checkModelName(nameField, this::translate, box()));
		createModel.onExecute(e -> createModel());
	}

	private void selectModelsGroup(SelectionEvent event) {
		String selected = event.selection().isEmpty() ? "allModelsOption" : (String) event.selection().getFirst();
		if (selected.equals("publicModelsOption")) refreshModels("Public models", new ModelsDatasource(box(), session(), false));
		else if (selected.equals("privateModelsOption")) refreshModels("Private models", new ModelsDatasource(box(), session(), true));
		else refreshModels("All models", new ModelsDatasource(box(), session(), null));
	}

	private void refreshModels(String title, ModelsDatasource source) {
		modelsCatalog.title(title);
		modelsCatalog.source(source);
		modelsCatalog.refresh();
	}

	private void notifyOpeningModel(Model model) {
		bodyBlock.hide();
		openingModelMessage.value(String.format(translate("Opening %s"), model.title()));
		openingModelBlock.show();
	}

	private void refreshAddModelDialog() {
		nameField.value(ModelHelper.proposeName());
		languageField.valueProvider(l -> ((Language)l).id());
		languageField.source(new LanguagesDatasource(box(), session()));
		languageTable.onAddItem(this::refreshLanguage);
	}

	private void refreshLanguage(AddItemEvent event) {
		Language language = event.item();
		LanguageTableRow row = event.component();
		row.ltNameItem.name.value(language.name());
		row.ltVersionItem.version.value(language.version());
		row.ltOwnerItem.owner.value(language.owner());
	}

	private void createModel() {
		if (!DisplayHelper.checkModelName(nameField, this::translate, box())) return;
		if (!DisplayHelper.check(titleField, this::translate)) return;
		if (!DisplayHelper.check(languageField)) {
			notifyUser("Language field is required", UserMessage.Type.Warning);
			return;
		}
		addModelDialog.close();
		String languageId = ((Language)languageField.selection().getFirst()).id();
		Model model = box().commands(ModelCommands.class).create(nameField.value(), titleField.value(), languageId, DisplayHelper.user(session()), username());
		notifyOpeningModel(model);
		DelayerUtil.execute(this, v -> notifier.redirect(PathHelper.modelUrl(session(), model)), 600);
		modelsCatalog.refresh();
	}

}