package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.AddCollectionItemEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.ModelCommands;
import io.quassar.editor.box.ui.datasources.ModelsDatasource;
import io.quassar.editor.box.ui.displays.rows.ModelTableRow;
import io.quassar.editor.box.ui.types.LanguageView;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

public class ModelsTemplate extends AbstractModelsTemplate<EditorBox> {
	private Language language;
	private LanguageView view;

	public ModelsTemplate(EditorBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void view(LanguageView view) {
		this.view = view;
	}

	@Override
	public void init() {
		super.init();
		addModelTrigger.onExecute(e -> openAddModelDialog());
		modelDialog.onCreate(this::open);
		modelTable.onAddItem(this::refresh);
		modelPublishDialog.onPublish((l, v) -> open(l));
	}

	@Override
	public void refresh() {
		super.refresh();
		addModelTrigger.visible(view == LanguageView.OwnerModels);
		modelTable.source(new ModelsDatasource(box(), session(), language, view));
		modelTable.reload();
	}

	private void refresh(AddCollectionItemEvent event) {
		refresh(event.item(), event.component());
	}

	private void refresh(Model model, ModelTableRow row) {
		row.modelLabelItem.label.title(ModelHelper.label(model, language(), box()));
		row.modelLabelItem.label.address(path -> PathHelper.modelPath(path, model));
		row.modelOwnerItem.owner.value(model.owner());
		row.operationsItem.operationsToolbar.visible(true);
		row.operationsItem.removeModelTrigger.onExecute(e -> removeModel(model));
		row.operationsItem.settingsModelTrigger.onExecute(e -> openSettingsDialog(model, row));
		row.operationsItem.publishModelTrigger.readonly(!PermissionsHelper.canPublish(model, session(), box()));
		row.operationsItem.publishModelTrigger.onExecute(e -> publishModel(model));
	}

	private void open(Model model) {
		notifier.dispatch(PathHelper.modelPath(model));
	}

	private void openSettingsDialog(Model model, ModelTableRow row) {
		modelSettingsDialog.onSave(e -> refresh(model, row));
		modelSettingsDialog.model(model);
		modelSettingsDialog.open();
	}

	private void open(Language language) {
		notifier.dispatch(PathHelper.languagePath(language));
	}

	private void openAddModelDialog() {
		modelDialog.language(language);
		modelDialog.open();
	}

	private void removeModel(Model model) {
		box().commands(ModelCommands.class).remove(model, username());
		refresh();
	}

	private void publishModel(Model model) {
		modelPublishDialog.model(model);
		modelPublishDialog.open();
	}

}