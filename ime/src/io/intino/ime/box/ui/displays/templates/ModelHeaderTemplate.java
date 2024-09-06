package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.utils.DelayerUtil;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.ModelCommands;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.model.Language;
import io.intino.ime.model.LanguageInfo;
import io.intino.ime.model.Model;

import java.util.function.Consumer;

public class ModelHeaderTemplate extends AbstractModelHeaderTemplate<ImeBox> {
	private Model model;
	private String _title;
	private String _description = null;
	private Consumer<Model> openModelListener;

	public ModelHeaderTemplate(ImeBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
	}

	public void title(String title) {
		this._title = title;
	}

	public void description(String value) {
		this._description = value;
	}

	public void onOpenModel(Consumer<Model> listener) {
		this.openModelListener = listener;
	}

	@Override
	public void init() {
		super.init();
		cancel.onExecute(e -> hideTitleEditor());
		save.onExecute(e -> saveTitle());
		titleField.onEnterPress(e -> saveTitle());
		models.visible(session().user() != null);
		user.visible(session().user() != null);
		versionsDialog.onOpen(e -> refreshLanguageVersionsDialog());
		settingsEditor.onSaveTitle(this::updateTitle);
		cloneModelEditor.onClone(this::open);
		removeModel.onExecute(e -> removeModel());
	}

	private void refreshLanguageVersionsDialog() {
		versionsCatalog.model(model);
		versionsCatalog.refresh();
	}

	@Override
	public void refresh() {
		super.refresh();
		titleLink.title(_title);
		titleLink.onExecute(e -> showTitleEditor());
		description.value(_description);
		if (session().user() == null) return;
		Language language = ModelHelper.language(model, box());
		languageVersions.visible(language.level() != LanguageInfo.Level.L1);
		models.path(PathHelper.modelsPath(session()));
		myModels.path(PathHelper.modelsPath(session()));
		settingsEditor.model(model);
		settingsEditor.mode(ModelSettingsEditor.Mode.Large);
		settingsEditor.refresh();
		removeModel.visible(!model.isTemporal());
		cloneModelEditor.model(model);
		cloneModelEditor.mode(CloneModelEditor.Mode.Large);
		cloneModelEditor.refresh();
	}

	private void open(Model model) {
		openModelListener.accept(model);
		DelayerUtil.execute(this, v -> notifier.redirect(PathHelper.modelUrl(session(), model)), 600);
	}

	private void showTitleEditor() {
		titleField.value(_title);
		titleLink.visible(false);
		titleEditor.visible(true);
		titleField.focus();
	}

	private void hideTitleEditor() {
		titleLink.visible(true);
		titleEditor.visible(false);
	}

	private void saveTitle() {
		_title = titleField.value();
		box().commands(ModelCommands.class).saveTitle(model, _title, username());
		hideTitleEditor();
		refresh();
	}

	private void updateTitle(String title) {
		_title = title;
		refresh();
	}

	private void removeModel() {
		box().commands(ModelCommands.class).remove(model, username());
		notifier.redirect(PathHelper.homeUrl(session()));
	}


}