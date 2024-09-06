package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.AddItemEvent;
import io.intino.alexandria.ui.utils.DelayerUtil;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.ModelCommands;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.ui.datasources.ModelsDatasource;
import io.intino.ime.box.ui.displays.rows.ModelTableRow;
import io.intino.ime.model.Model;

import java.util.function.Consumer;

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
		row.modelTitleItem.title.title(model.title());
		row.modelTitleItem.title.onExecute(e -> {
			openModelListener.accept(model);
			DelayerUtil.execute(this, v -> notifier.redirect(PathHelper.modelUrl(session(), model)), 600);
		});
		row.modelOwnerItem.owner.value(model.owner().fullName());
		row.lastModifiedDateItem.lastModifiedDate.value(model.lastModifyDate());
		row.operationsItem.cloneModelEditor.model(model);
		row.operationsItem.cloneModelEditor.mode(CloneModelEditor.Mode.Small);
		row.operationsItem.cloneModelEditor.onClone(e -> refresh());
		row.operationsItem.cloneModelEditor.refresh();
		row.operationsItem.settingsEditor.model(model);
		row.operationsItem.settingsEditor.onSaveTitle(title -> row.modelTitleItem.title.title(title));
		row.operationsItem.settingsEditor.mode(ModelSettingsEditor.Mode.Small);
		row.operationsItem.settingsEditor.refresh();
		row.operationsItem.removeModel.onExecute(e -> remove(model));
	}

	private void remove(Model model) {
		box().commands(ModelCommands.class).remove(model, username());
		refresh();
	}

}