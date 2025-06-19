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

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class ModelSettingsEditor extends AbstractModelSettingsEditor<EditorBox> {
	private Model model;
	private Consumer<Model> saveTitleListener;
	private Consumer<Model> cloneListener;
	private Consumer<Model> updateLanguageVersionListener;
	private String release;

	public ModelSettingsEditor(EditorBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
	}

	public void release(String release) {
		this.release = release;
	}

	public void onSaveTitle(Consumer<Model> listener) {
		this.saveTitleListener = listener;
	}

	public void onClone(Consumer<Model> listener) {
		this.cloneListener = listener;
	}

	public void onUpdateLanguageVersion(Consumer<Model> listener) {
		this.updateLanguageVersionListener = listener;
	}

	@Override
	public void init() {
		super.init();
		generalBlock.onInit(e -> initGeneralBlock());
		generalBlock.onShow(e -> refreshGeneralBlock());
		collaboratorsBlock.onInit(e -> initCollaboratorsBlock());
		collaboratorsBlock.onShow(e -> refreshCollaboratorsBlock());
	}

	@Override
	public void refresh() {
		super.refresh();
		if (model.isExample()) settingsTabSelector.hideOption("collaboratorsOption");
		settingsTabSelector.select(0);
	}

	private void initGeneralBlock() {
		removeModel.onExecute(e -> removeModel());
		cloneModel.onExecute(e -> cloneModel());
		editTitle.onExecute(e -> openTitleDialog());
		titleDialog.onSave(e -> {
			modelTitleField.value(e);
			saveTitleListener.accept(model);
		});
		modelDescriptionField.onChange(e -> saveDescription());
		languageSelector.onSelect(e -> saveLanguage());
		accessTypeField.onToggle(e -> saveAccessType());
	}

	private void refreshGeneralBlock() {
		Language language = box().languageManager().get(model.language());
		boolean canRemove = PermissionsHelper.canRemove(model, session(), box());
		editTitle.readonly(!PermissionsHelper.canEditTitle(model, box()));
		modelTitleField.value(ModelHelper.label(model, language(), box()));
		modelDescriptionField.value(model.description());
		modelDescriptionField.readonly(model.isTemplate());
		languageName.value(model.language().languageId());
		languageSelector.clear();
		languageSelector.addAll(language.releases().stream().map(LanguageRelease::version).toList().reversed());
		languageSelector.selection(model.language().version());
		languageSelector.readonly(model.isExample() || model.isTemplate());
		removeModel.readonly(!canRemove);
		removeModel.formats(Set.of("airRight", "whiteColor", canRemove ? "redBackground" : "disabledButton"));
		generalBlock.cloneModelBlock.visible(!model.isTemplate() && !model.isExample());
		cloneModel.readonly(!PermissionsHelper.canClone(model, release, session(), box()));
		refreshAccessTypeBlock();
	}

	private void refreshAccessTypeBlock() {
		generalBlock.accessTypeBlock.visible(false);
		if (!generalBlock.accessTypeBlock.isVisible()) return;
		accessTypeField.state(model.isPrivate() ? ToggleEvent.State.On : ToggleEvent.State.Off);
		accessTypeField.readonly(model.isExample() || model.isTemplate());
	}

	private void initCollaboratorsBlock() {
		collaboratorsStamp.onChange(this::saveCollaborators);
	}

	private void refreshCollaboratorsBlock() {
		collaboratorsStamp.owner(model.owner());
		collaboratorsStamp.collaborators(model.collaborators());
		collaboratorsStamp.refresh();
	}

	private void saveDescription() {
		box().commands(ModelCommands.class).saveDescription(model, modelDescriptionField.value(), username());
	}

	private void saveAccessType() {
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

	private void saveCollaborators(List<String> collaborators) {
		box().commands(ModelCommands.class).save(model, collaborators, username());
	}

	private void removeModel() {
		notifyUser(translate("Removing model..."), UserMessage.Type.Loading);
		String language = model.language().artifactId();
		box().commands(ModelCommands.class).remove(model, username());
		hideUserNotification();
		if (model.isExample()) doClose();
		else notifier.dispatch(PathHelper.languagePath(language));
	}

	private void doClose() {
		closeTrigger.launch();
		box().souls().stream().filter(Objects::nonNull).map(s -> s.displays(LanguageKitTemplate.class)).flatMap(Collection::stream).distinct().forEach(d -> d.notifyRemove(model));
	}

	private void cloneModel() {
		cloneListener.accept(model);
	}

	private void openTitleDialog() {
		titleDialog.model(model);
		titleDialog.open();
	}

}