package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.Resource;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.displays.events.ChangeEvent;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class ModelSettingsDialog extends AbstractModelSettingsDialog<EditorBox> {
	private Model model;
	private Set<String> tagSet;
	private Consumer<Model> renameListener;
	private Consumer<Model> saveListener;

	public ModelSettingsDialog(EditorBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
	}

	public void onRename(Consumer<Model> listener) {
		this.renameListener = listener;
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
		languageBlock.onInit(e -> initLanguageBlock());
		languageBlock.onShow(e -> refreshLanguageBlock());
		collaboratorsBlock.onInit(e -> initCollaboratorsBlock());
		collaboratorsBlock.onShow(e -> refreshCollaboratorsBlock());
		dialog.onOpen(e -> refreshDialog());
		saveSettings.onExecute(e -> saveSettings());
		addTagDialog.onOpen(e -> refreshTagDialog());
	}

	private void refreshDialog() {
		Language language = box().languageManager().get(model.name());
		if (language == null) settingsTabSelector.hideOption("languageOption");
		settingsTabSelector.hideOption("collaboratorsOption");
		settingsTabSelector.select(0);
	}

	private void initGeneralBlock() {
		modelNameField.onChange(e -> checkModelName());
		removeModel.onExecute(e -> removeModel());
	}

	private void refreshGeneralBlock() {
		modelNameField.value(model.name());
		modelTitleField.value(ModelHelper.label(model, language(), box()));
		modelDescriptionField.value(model.description());
		modelAccessTypeField.state(model.isPrivate() ? ToggleEvent.State.On : ToggleEvent.State.Off);
		removeModel.readonly(!PermissionsHelper.canRemove(model, session(), box()));
	}

	private void initLanguageBlock() {
		logoField.onChange(this::updateLogo);
		addTag.onExecute(e -> addTag());
		tagField.onEnterPress(e -> addTag());
	}

	private void refreshLanguageBlock() {
		Language language = box().languageManager().get(model.name());
		tagSet = new HashSet<>(language.tags());
		File logo = box().archetype().languages().logo(language.name());
		logoField.value(logo.exists() ? logo : null);
		languageLevelSelector.select(language.level() == Language.Level.L1 ? "level1Option" : "level2Option");
		refreshTags();
	}

	private void refreshTags() {
		languageTags.clear();
		tagSet.stream().sorted(Comparator.naturalOrder()).forEach(o -> fill(o, languageTags.add()));
	}

	private void fill(String tag, TagEditor display) {
		display.tag(tag);
		display.onRemove(o -> removeTag(tag));
		display.refresh();
	}

	private void removeTag(String tag) {
		tagSet.remove(tag);
		refreshTags();
	}

	private void addTag() {
		if (!DisplayHelper.check(tagField, this::translate)) return;
		addTagDialog.close();
		tagSet.add(tagField.value());
		refreshTags();
	}

	private void refreshTagDialog() {
		tagField.value(null);
	}

	private void initCollaboratorsBlock() {
	}

	private void refreshCollaboratorsBlock() {
	}

	private void saveSettings() {
		if (!check()) return;
		dialog.close();
		boolean renamed = !model.name().equals(modelNameField.value());
		saveModel();
		saveLanguage();
		saveListener.accept(model);
		if (renamed) renameListener.accept(model);
	}

	private boolean check() {
		if (!checkModel()) return false;
		if (!checkLanguage()) return false;
		return true;
	}

	private boolean checkModel() {
		if (!checkModelName()) return false;
		if (!DisplayHelper.check(modelTitleField, this::translate)) return false;
		return DisplayHelper.check(modelDescriptionField, this::translate);
	}

	private boolean checkModelName() {
		Language language = box().languageManager().get(model.language());
		return DisplayHelper.checkLanguageName(modelNameField, language, this::translate, box());
	}

	private boolean checkLanguage() {
		String languageName = Language.nameOf(model.name());
		if (!box().languageManager().exists(languageName)) return true;
		if (!languageBlock.isVisible()) return true;
		String language = Language.nameOf(model.name());
		boolean hasLogo = box().archetype().languages().logo(language).exists();
		if (box().languageManager().exists(language) && !hasLogo && !logoFile().exists()) {
			notifyUser(translate("Select logo"), UserMessage.Type.Warning);
			return false;
		}
		return true;
	}

	private void saveModel() {
		box().commands(ModelCommands.class).save(model, modelNameField.value(), modelTitleField.value(), modelDescriptionField.value(), username());
		boolean isPrivate = modelAccessTypeField.state() == ToggleEvent.State.On;
		saveAccessType(isPrivate);
	}

	private void saveAccessType(boolean isPrivate) {
		if (isPrivate) box().commands(ModelCommands.class).makePrivate(model, username());
		else box().commands(ModelCommands.class).makePublic(model, username());
	}

	private void removeModel() {
		notifyUser(translate("Removing model..."), UserMessage.Type.Loading);
		box().commands(ModelCommands.class).remove(model, username());
		notifier.dispatch(PathHelper.languagePath(model.language()));
	}

	private void saveLanguage() {
		String languageName = Language.nameOf(model.name());
		if (!box().languageManager().exists(languageName)) return;
		Language language = box().languageManager().get(languageName);
		box().commands(LanguageCommands.class).save(language, model.description(), level(), new ArrayList<>(tagSet), logoFile(), username());
	}

	private Language.Level level() {
		String selected = languageLevelSelector.selection().getFirst();
		if (selected.equals("level1Option")) return Language.Level.L1;
		return Language.Level.L2;
	}

	private void updateLogo(ChangeEvent event) {
		try {
			File tmpFile = logoFile();
			if (tmpFile.exists()) tmpFile.delete();
			Resource value = event.value();
			if (value != null) Files.write(tmpFile.toPath(), value.bytes());
			logoField.value(value != null ? tmpFile : null);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private File logoFile() {
		return new File(box().archetype().tmp().root(), model.name() + "-logo.png");
	}

}