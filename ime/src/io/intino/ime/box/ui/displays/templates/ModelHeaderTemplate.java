package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.SelectionEvent;
import io.intino.alexandria.ui.utils.DelayerUtil;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.LanguageCommands;
import io.intino.ime.box.commands.ModelCommands;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.box.util.VersionNumberComparator;
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
		settingsEditor.onSaveTitle(this::updateTitle);
		cloneModelEditor.onClone(this::open);
		createLanguage.onExecute(e -> createLanguage());
		removeLanguage.onExecute(e -> removeLanguage());
		removeModel.onExecute(e -> removeModel());
		login.onExecute(e -> {
			session().add("callback", session().browser().requestUrl());
			notifier.redirect(session().login(session().browser().baseUrl()));
		});
		versionSelector.onSelect(this::openVersion);
		createVersionDialog.onOpen(e -> refreshCreateVersionDialog());
		createVersion.onExecute(e -> createVersion());
	}

	@Override
	public void refresh() {
		super.refresh();
		refreshVersions();
		login.visible(user() == null);
		titleLink.title(_title);
		titleLink.onExecute(e -> showTitleEditor());
		description.value(_description);
		if (session().user() == null) return;
		models.path(PathHelper.modelsPath(session()));
		myModels.path(PathHelper.modelsPath(session()));
		settingsEditor.model(model);
		settingsEditor.mode(ModelSettingsEditor.Mode.Large);
		settingsEditor.refresh();
		removeModel.visible(!model.isTemporal());
		cloneModelEditor.model(model);
		cloneModelEditor.mode(CloneModelEditor.Mode.Large);
		cloneModelEditor.refresh();
		boolean existsLanguage = box().languageManager().exists(model.id());
		createLanguage.visible(!existsLanguage);
		removeLanguage.visible(existsLanguage);
	}

	private void refreshVersions() {
		Language language = ModelHelper.language(model, box());
		versionSelector.clear();
		versionSelector.addAll(model.versions().stream().map(Model.Version::id).sorted((o1, o2) -> VersionNumberComparator.getInstance().compare(o2, o1)).toList());
		versionSelector.selection(model.version());
		versionBlock.visible(language.level() != LanguageInfo.Level.L1 && user() != null && model.isPrivate());
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

	private void openVersion(SelectionEvent event) {
		String version = event.selection().isEmpty() ? null : (String) event.selection().getFirst();
		if (version == null || version.equals(model.version())) return;
		open(Model.clone(model).version(version));
	}

	private void refreshCreateVersionDialog() {
		versionEditor.model(model);
		versionEditor.createMode(true);
		versionEditor.onAccept(e -> createVersion());
		versionEditor.refresh();
	}

	private void createVersion() {
		if (!versionEditor.check()) return;
		createVersionDialog.close();
		open(box().commands(ModelCommands.class).createVersion(model, versionEditor.version(), username()));
	}

	private void createLanguage() {
		Language language = box().commands(LanguageCommands.class).create(model, model.versionMap().get(model.version()), username());
		box().commands(LanguageCommands.class).publish(language, username());
		refresh();
	}

	private void removeLanguage() {
		Language language = box().languageManager().get(model.id());
		box().commands(LanguageCommands.class).unPublish(language, username());
		box().commands(LanguageCommands.class).remove(language, username());
		refresh();
	}

}