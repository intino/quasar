package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.displays.events.actionable.ToggleEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.LanguageCommands;
import io.quassar.editor.box.commands.ModelCommands;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;
import io.quassar.editor.model.User;

import java.util.List;
import java.util.function.Consumer;

public class ModelSettingsDialog extends AbstractModelSettingsDialog<EditorBox> {
	private Model model;
	private Consumer<Model> saveListener;
	private Boolean accessType = null;
	private List<User> collaboratorList = null;
	private String token;

	public ModelSettingsDialog(EditorBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
	}

	public void onSave(Consumer<Model> listener) {
		this.saveListener = listener;
	}

	public void open() {
		dialog.open();
	}

	@Override
	public void init() {
		super.init();
		generalBlock.onInit(e -> initGeneralBlock());
		generalBlock.onShow(e -> refreshGeneralBlock());
		collaboratorsBlock.onInit(e -> initCollaboratorsBlock());
		collaboratorsBlock.onShow(e -> refreshCollaboratorsBlock());
		dialog.onOpen(e -> refreshDialog());
		saveSettings.onExecute(e -> saveSettings());
	}

	private void refreshDialog() {
		dialog.title(translate("Information of %s").formatted(ModelHelper.label(model, language(), box())));
		Language language = box().languageManager().get(model);
		if (language == null) settingsTabSelector.hideOption("languageOption");
		settingsTabSelector.select(0);
	}

	private void initGeneralBlock() {
		removeModel.onExecute(e -> removeModel());
	}

	private void refreshGeneralBlock() {
		modelTitleField.value(ModelHelper.label(model, language(), box()));
		modelDescriptionField.value(model.description());
		removeModel.readonly(!PermissionsHelper.canRemove(model, session(), box()));
		accessType = model.isPrivate();
		accessTypeField.state(model.isPrivate() ? ToggleEvent.State.On : ToggleEvent.State.Off);
	}

	private void initCollaboratorsBlock() {
		collaboratorsStamp.onChange(e -> this.collaboratorList = e);
	}

	private void refreshCollaboratorsBlock() {
		collaboratorsStamp.model(model);
		collaboratorsStamp.refresh();
	}

	private void saveSettings() {
		if (!check()) return;
		dialog.close();
		saveModel();
		saveLanguageProperties();
		saveListener.accept(model);
	}

	private boolean check() {
		if (!DisplayHelper.check(modelTitleField, this::translate)) return false;
		return DisplayHelper.check(modelDescriptionField, this::translate);
	}

	private void saveModel() {
		box().commands(ModelCommands.class).saveProperties(model, modelTitleField.value(), modelDescriptionField.value(), username());
		saveAccessType();
		saveCollaborators();
	}

	private void saveAccessType() {
		if (accessType == null) return;
		boolean isPrivate = accessTypeField.state() == ToggleEvent.State.On;
		if (isPrivate) box().commands(ModelCommands.class).makePrivate(model, username());
		else box().commands(ModelCommands.class).makePublic(model, username());
	}

	private void saveCollaborators() {
		if (collaboratorList == null) return;
		box().commands(ModelCommands.class).save(model, collaboratorList, username());
	}

	private void removeModel() {
		notifyUser(translate("Removing model..."), UserMessage.Type.Loading);
		box().commands(ModelCommands.class).remove(model, username());
		hideUserNotification();
		notifier.dispatch(PathHelper.languagePath(model.language().artifactId()));
	}

	private void saveLanguageProperties() {
		Language language = box().languageManager().get(model);
		if (language == null) return;
		box().commands(LanguageCommands.class).saveProperties(language, model.title(), model.description(), username());
	}

}