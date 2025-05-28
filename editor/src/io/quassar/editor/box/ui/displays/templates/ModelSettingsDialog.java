package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.displays.events.actionable.ToggleEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.ModelCommands;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import io.quassar.editor.model.Model;
import io.quassar.editor.model.User;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class ModelSettingsDialog extends AbstractModelSettingsDialog<EditorBox> {
	private Model model;
	private Consumer<Model> saveListener;
	private Consumer<Model> cloneListener;
	private Consumer<Model> updateLanguageVersionListener;
	private Boolean accessType = null;
	private List<User> collaboratorList = null;
	private String release;

	public ModelSettingsDialog(EditorBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
	}

	public void release(String release) {
		this.release = release;
	}

	public void onSave(Consumer<Model> listener) {
		this.saveListener = listener;
	}

	public void onClone(Consumer<Model> listener) {
		this.cloneListener = listener;
	}

	public void onUpdateLanguageVersion(Consumer<Model> listener) {
		this.updateLanguageVersionListener = listener;
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
		cloneModel.onExecute(e -> cloneModel());
		editTitle.onExecute(e -> openTitleDialog());
		titleDialog.onSave(e -> modelTitleField.value(e));
	}

	private void refreshGeneralBlock() {
		Language language = box().languageManager().get(model.language());
		boolean canRemove = PermissionsHelper.canRemove(model, session(), box());
		editTitle.readonly(!PermissionsHelper.canEditTitle(model, box()));
		modelTitleField.value(ModelHelper.label(model, language(), box()));
		modelDescriptionField.value(model.description());
		languageName.value(model.language().languageId());
		languageSelector.clear();
		languageSelector.addAll(language.releases().stream().map(LanguageRelease::version).toList().reversed());
		languageSelector.selection(model.language().version());
		removeModel.readonly(!canRemove);
		removeModel.formats(Set.of("airRight", "whiteColor", canRemove ? "redBackground" : "greyHardBackground"));
		cloneModel.readonly(!PermissionsHelper.canClone(model, release, session(), box()));
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
		saveListener.accept(model);
	}

	private boolean check() {
		return true;
	}

	private void saveModel() {
		box().commands(ModelCommands.class).saveDescription(model, modelDescriptionField.value(), username());
		saveAccessType();
		saveLanguage();
		saveCollaborators();
	}

	private void saveAccessType() {
		if (accessType == null) return;
		boolean isPrivate = accessTypeField.state() == ToggleEvent.State.On;
		if (isPrivate) box().commands(ModelCommands.class).makePrivate(model, username());
		else box().commands(ModelCommands.class).makePublic(model, username());
	}

	private void saveLanguage() {
		List<String> selection = languageSelector.selection();
		String selected = !selection.isEmpty() ? selection.getFirst() : null;
		if (model.language().version().equals(selected)) return;
		box().commands(ModelCommands.class).updateLanguageVersion(model, selected, username());
		updateLanguageVersionListener.accept(model);
	}

	private void saveCollaborators() {
		if (collaboratorList == null) return;
		box().commands(ModelCommands.class).save(model, collaboratorList, username());
	}

	private void removeModel() {
		notifyUser(translate("Removing model..."), UserMessage.Type.Loading);
		String language = model.language().artifactId();
		box().commands(ModelCommands.class).remove(model, username());
		hideUserNotification();
		notifier.dispatch(PathHelper.languagePath(language));
	}

	private void cloneModel() {
		dialog.close();
		cloneListener.accept(model);
	}

	private void openTitleDialog() {
		titleDialog.model(model);
		titleDialog.open();
	}

}