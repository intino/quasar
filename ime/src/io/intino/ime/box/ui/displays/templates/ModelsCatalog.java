package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.displays.events.AddItemEvent;
import io.intino.alexandria.ui.displays.events.SelectionEvent;
import io.intino.alexandria.ui.utils.DelayerUtil;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.ModelCommands;
import io.intino.ime.box.languages.LanguageManager;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.ui.datasources.ModelsDatasource;
import io.intino.ime.box.ui.displays.rows.ModelTableRow;
import io.intino.ime.model.Model;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ModelsCatalog extends AbstractModelsCatalog<ImeBox> {
	private String _title;
	private ModelsDatasource source;
	private Consumer<Model> openModelListener;
	private Model selectedModel;

	public ModelsCatalog(ImeBox box) {
		super(box);
	}

	public void title(String title) {
		this._title = title;
	}

	public void source(ModelsDatasource source) {
		this.source = source;
	}

	public void onOpenModel(Consumer<Model> listener) {
		this.openModelListener = listener;
	}

	@Override
	public void init() {
		super.init();
		modelTable.onAddItem(this::refresh);
		removeSelection.onExecute(this::removeSelection);
	}

	@Override
	public void refresh() {
		super.refresh();
		title.value(translate(_title));
		modelTable.source(source);
	}

	private void refresh(AddItemEvent event) {
		Model model = event.item();
		ModelTableRow row = event.component();
		row.modelTitleItem.accessType.visible(!model.isPrivate());
		row.modelTitleItem.title.title(model.title());
		row.modelTitleItem.title.onExecute(e -> {
			openModelListener.accept(model);
			DelayerUtil.execute(this, v -> notifier.redirect(PathHelper.modelUrl(session(), model)), 600);
		});
		row.modelOwnerItem.owner.value(model.owner().fullName());
		row.modelLanguageItem.language.value(model.language());
		row.lastModifiedDateItem.lastModifiedDate.value(model.lastModifyDate());
		row.operationsItem.cloneModelEditor.model(model);
		row.operationsItem.cloneModelEditor.mode(CloneModelEditor.Mode.Small);
		row.operationsItem.cloneModelEditor.onClone(e -> refresh());
		row.operationsItem.cloneModelEditor.refresh();
		row.operationsItem.settingsEditor.model(model);
		row.operationsItem.settingsEditor.onSaveTitle(title -> row.modelTitleItem.title.title(title));
		row.operationsItem.settingsEditor.onSaveAccessType(isPrivate -> row.modelTitleItem.accessType.visible(!isPrivate));
		row.operationsItem.settingsEditor.mode(ModelSettingsEditor.Mode.Small);
		row.operationsItem.settingsEditor.refresh();
		row.operationsItem.removeModel.onExecute(e -> remove(model));
	}

	private void remove(Model model) {
		if (!box().languageManager().versions(model.name()).isEmpty()) {
			notifyUser(translate("Model has versions"), UserMessage.Type.Error);
			return;
		}
		box().commands(ModelCommands.class).remove(model, username());
		refresh();
	}

	private void removeSelection(SelectionEvent event) {
		List<Model> selection = event.selection();
		LanguageManager languageManager = box().languageManager();
		notifyUser(translate("Removing selected models"), UserMessage.Type.Loading);
		List<Model> modelsWithVersions = selection.stream().filter(m -> !languageManager.versions(m.name()).isEmpty()).toList();
		List<Model> modelsWithoutVersions = selection.stream().filter(m -> languageManager.versions(m.name()).isEmpty()).toList();
		modelsWithoutVersions.forEach(this::remove);
		if (modelsWithVersions.isEmpty()) notifyUser(translate("Models removed"), UserMessage.Type.Success);
		else if (selection.size() == modelsWithVersions.size()) notifyUser(translate("Cannot remove models with versions"), UserMessage.Type.Error);
		else notifyUser(translate("Removed models, but this models with versions could not be removed: " + modelsWithVersions.stream().map(Model::title).collect(Collectors.joining(", "))), UserMessage.Type.Success);
		refresh();
	}

}