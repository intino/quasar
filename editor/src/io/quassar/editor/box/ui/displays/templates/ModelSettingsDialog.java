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
import io.quassar.editor.model.User;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;

public class ModelSettingsDialog extends AbstractModelSettingsDialog<EditorBox> {
	private Model model;
	private Set<String> tagSet;
	private Map<String, String> tokensMap;
	private Consumer<Model> renameListener;
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
		accessBlock.onInit(e -> initAccessBlock());
		accessBlock.onShow(e -> refreshAccessBlock());
		dialog.onOpen(e -> refreshDialog());
		saveSettings.onExecute(e -> saveSettings());
		addTagDialog.onOpen(e -> refreshTagDialog());
		addToken.onExecute(e -> addToken());
		generateToken.onExecute(e -> generateToken());
		addTokenDialog.onOpen(e -> refreshTokenDialog());
	}

	private void refreshDialog() {
		Language language = box().languageManager().get(model.name());
		if (language == null) settingsTabSelector.hideOption("languageOption");
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
		removeModel.readonly(!PermissionsHelper.canRemove(model, session(), box()));
	}

	private void initLanguageBlock() {
		logoField.onChange(this::updateLogo);
		addTag.onExecute(e -> addTag());
		tagField.onEnterPress(e -> addTag());
		removeLanguage.onExecute(e -> removeLanguage());
	}

	private void refreshLanguageBlock() {
		Language language = box().languageManager().get(model.name());
		tagSet = new HashSet<>(language.tags());
		File logo = box().archetype().languages().logo(language.name());
		logoField.value(logo.exists() ? logo : null);
		languageFileExtensionField.value(language.fileExtension());
		languageLevelSelector.select(language.level() == Language.Level.L1 ? "level1Option" : "level2Option");
		refreshTags();
		removeLanguage.readonly(!PermissionsHelper.canRemove(language, session(), box()));
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
		collaboratorsStamp.onChange(e -> this.collaboratorList = e);
	}

	private void refreshCollaboratorsBlock() {
		collaboratorsStamp.model(model);
		collaboratorsStamp.refresh();
	}

	private void initAccessBlock() {
		modelAccessTypeField.onToggle(e -> accessBlock.tokensBlock.visible(e.state() == ToggleEvent.State.On));
		applicationField.onEnterPress(e -> addToken());
	}

	private void refreshAccessBlock() {
		accessType = model.isPrivate();
		modelAccessTypeField.state(model.isPrivate() ? ToggleEvent.State.On : ToggleEvent.State.Off);
		accessBlock.tokensBlock.visible(model.isPrivate());
		tokensMap = new HashMap<>(model.tokens());
		refreshTokens();
	}

	private void refreshTokens() {
		tokens.clear();
		tokensMap.keySet().stream().sorted().forEach(e -> fill(e, tokens.add()));
	}

	private void fill(String app, TokenEditor display) {
		display.app(app);
		display.onRemove(o -> removeToken(app));
		display.refresh();
	}

	private void removeToken(String app) {
		tokensMap.remove(app);
		refreshTokens();
	}

	private void addToken() {
		if (!DisplayHelper.check(applicationField, this::translate)) return;
		if (token == null || token.isEmpty()) {
			notifyUser("Generate token to continue", UserMessage.Type.Warning);
			return;
		}
		addTokenDialog.close();
		tokensMap.put(applicationField.value(), tokenField.value());
		refreshTokens();
	}

	private void generateToken() {
		this.token = ModelHelper.proposeToken();
		tokenField.value(token);
		copyToken.text(token);
	}

	private void refreshTokenDialog() {
		if (token == null) this.token = ModelHelper.proposeToken();
		applicationField.value(null);
		tokenField.value(token);
		copyToken.text(token);
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
		if (model.name().equals(modelNameField.value())) return true;
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
		return DisplayHelper.check(languageFileExtensionField, this::translate);
	}

	private void saveModel() {
		box().commands(ModelCommands.class).save(model, modelNameField.value(), modelTitleField.value(), modelDescriptionField.value(), username());
		saveAccessType();
		saveCollaborators();
	}

	private void saveAccessType() {
		if (accessType == null) return;
		boolean isPrivate = modelAccessTypeField.state() == ToggleEvent.State.On;
		if (isPrivate) box().commands(ModelCommands.class).makePrivate(model, username());
		else box().commands(ModelCommands.class).makePublic(model, username());
		saveTokens();
	}

	private void saveTokens() {
		if (tokensMap == null) return;
		box().commands(ModelCommands.class).save(model, tokensMap, username());
	}

	private void saveCollaborators() {
		if (collaboratorList == null) return;
		box().commands(ModelCommands.class).save(model, collaboratorList, username());
	}

	private void removeLanguage() {
		notifyUser(translate("Removing language..."), UserMessage.Type.Loading);
		Language language = box().languageManager().get(model.name());
		box().commands(LanguageCommands.class).remove(language, username());
		notifyUser(translate("Language removed successfully..."), UserMessage.Type.Success);
		dialog.close();
	}

	private void removeModel() {
		notifyUser(translate("Removing model..."), UserMessage.Type.Loading);
		box().commands(ModelCommands.class).remove(model, username());
		hideUserNotification();
		notifier.dispatch(PathHelper.languagePath(model.language()));
	}

	private void saveLanguage() {
		String languageName = Language.nameOf(model.name());
		if (!box().languageManager().exists(languageName)) return;
		Language language = box().languageManager().get(languageName);
		String fileExtension = languageFileExtensionField.value();
		box().commands(LanguageCommands.class).save(language, model.description(), fileExtension, level(), new ArrayList<>(tagSet), logoFile(), username());
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