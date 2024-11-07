package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.displays.components.Grouping;
import io.intino.alexandria.ui.displays.events.AddCollectionItemEvent;
import io.intino.alexandria.ui.displays.events.SelectionEvent;
import io.intino.alexandria.ui.displays.notifiers.GroupingNotifier;
import io.intino.alexandria.ui.utils.DelayerUtil;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.ModelCommands;
import io.intino.ime.box.languages.LanguageManager;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.ui.datasources.ModelsDatasource;
import io.intino.ime.box.ui.displays.rows.ModelTableRow;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;
import io.intino.ime.model.Release;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ModelsCatalog extends AbstractModelsCatalog<ImeBox> {
	private String _title;
	private ModelsDatasource source;
	private Language language;
	private Consumer<Model> openModelListener;
	private boolean readonly = false;
	private boolean embedded = false;

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

	public void language(Language language) {
		this.language = language;
	}

	public void readonly(boolean value) {
		this.readonly = value;
	}

	public void embedded(boolean value) {
		this.embedded = value;
	}

	public void filter(String condition) {
		modelTable.filter(condition);
	}

	public long itemCount() {
		return modelTable.itemCount();
	}

	public void bindTo(Grouping< GroupingNotifier, ImeBox> grouping) {
		grouping.bindTo(modelTable);
	}

	@Override
	public void init() {
		super.init();
		modelDialog.onCreate(this::modelCreated);
		addModelTrigger.onExecute(e -> openAddModel());
		modelTable.onAddItem(this::refresh);
		removeSelection.onExecute(this::removeSelection);
		searchBox.onChange(e -> filter(e.value()));
		searchBox.onEnterPress(e -> filter(e.value()));
	}

	@Override
	public void refresh() {
		super.refresh();
		title.value(translate(_title));
		toolbar.visible(!readonly || embedded);
		addModelTrigger.visible(!readonly);
		removeSelection.visible(!readonly);
		searchBox.visible(embedded);
		modelTable.source(source);
	}

	private void refresh(AddCollectionItemEvent event) {
		Model model = event.item();
		ModelTableRow row = event.component();
		row.modelLabelItem.accessType.visible(!model.isPrivate());
		row.modelLabelItem.label.title(ModelHelper.label(model, language(), box()));
		row.modelLabelItem.label.onExecute(e -> {
			openModelListener.accept(model);
			DelayerUtil.execute(this, v -> notifier.redirect(PathHelper.modelUrl(session(), model)), 600);
		});
		row.modelOwnerItem.owner.value(model.owner());
		row.modelLanguageItem.language.value(model.modelingLanguage());
		row.operationsItem.cloneModelEditor.model(model);
		row.operationsItem.cloneModelEditor.mode(CloneModelDialog.Mode.Small);
		row.operationsItem.cloneModelEditor.view(CloneModelDialog.View.List);
		row.operationsItem.cloneModelEditor.onClone(e -> refresh());
		row.operationsItem.cloneModelEditor.refresh();
		row.operationsItem.settingsEditor.model(model);
		row.operationsItem.settingsEditor.onSaveAccessType(isPrivate -> row.modelLabelItem.accessType.visible(!isPrivate));
		row.operationsItem.settingsEditor.mode(ModelSettingsDialog.Mode.Small);
		row.operationsItem.settingsEditor.view(ModelSettingsDialog.View.List);
		row.operationsItem.settingsEditor.refresh();
		row.operationsItem.operationsToolbar.visible(!readonly);
		row.operationsItem.removeModel.readonly(!ModelHelper.canRemove(model, box()));
		row.operationsItem.removeModel.onExecute(e -> remove(model));
	}

	private void remove(Model model) {
		if (!ModelHelper.canRemove(model, box())) {
			notifyUser(translate("Model has releases"), UserMessage.Type.Error);
			return;
		}
		box().commands(ModelCommands.class).remove(model, username());
		refresh();
	}

	private void removeSelection(SelectionEvent event) {
		List<Model> selection = event.selection();
		LanguageManager languageManager = box().languageManager();
		notifyUser(translate("Removing selected models"), UserMessage.Type.Loading);
		List<Model> modelsWithReleases = selection.stream().filter(m -> !languageManager.releases(m).isEmpty()).toList();
		List<Model> modelsWithoutReleases = selection.stream().filter(m -> languageManager.releases(m).isEmpty()).toList();
		modelsWithoutReleases.forEach(this::remove);
		if (modelsWithReleases.isEmpty()) notifyUser(translate("Models removed"), UserMessage.Type.Success);
		else if (selection.size() == modelsWithReleases.size()) notifyUser(translate("Cannot remove models with releases"), UserMessage.Type.Error);
		else notifyUser(translate("Removed models without releases, but those models could not be removed: " + modelsWithReleases.stream().map(m -> ModelHelper.label(m, language(), box())).collect(Collectors.joining(", "))), UserMessage.Type.Success);
		refresh();
	}

	private void modelCreated(Model model) {
		open(model);
		modelTable.reload();
	}

	private Release lastRelease(Language language) {
		return box().languageManager().lastRelease(language);
	}

	private void open(Model model) {
		openModelListener.accept(model);
		DelayerUtil.execute(this, v -> notifier.redirect(PathHelper.modelUrl(session(), model)), 600);
	}

	private void openAddModel() {
		modelDialog.language(language);
		modelDialog.release(lastRelease(language));
		modelDialog.open();
	}

}