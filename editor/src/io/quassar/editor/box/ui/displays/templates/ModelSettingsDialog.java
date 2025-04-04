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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ModelSettingsDialog extends AbstractModelSettingsDialog<EditorBox> {
	private Model model;
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
		collaboratorsBlock.onInit(e -> initCollaboratorsBlock());
		collaboratorsBlock.onShow(e -> refreshCollaboratorsBlock());
		accessBlock.onInit(e -> initAccessBlock());
		accessBlock.onShow(e -> refreshAccessBlock());
		dialog.onOpen(e -> refreshDialog());
		saveSettings.onExecute(e -> saveSettings());
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
		modelHintField.value(model.hint());
		modelDescriptionField.value(model.description());
		removeModel.readonly(!PermissionsHelper.canRemove(model, session(), box()));
	}

	private void initCollaboratorsBlock() {
		collaboratorsStamp.onChange(e -> this.collaboratorList = e);
	}

	private void refreshCollaboratorsBlock() {
		collaboratorsStamp.model(model);
		collaboratorsStamp.refresh();
	}

	private void initAccessBlock() {
		accessTypeField.onToggle(e -> accessBlock.tokensBlock.visible(e.state() == ToggleEvent.State.On));
		applicationField.onEnterPress(e -> addToken());
	}

	private void refreshAccessBlock() {
		accessType = model.isPrivate();
		accessTypeField.state(model.isPrivate() ? ToggleEvent.State.On : ToggleEvent.State.Off);
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
		saveLanguageProperties();
		saveListener.accept(model);
		if (renamed) renameListener.accept(model);
	}

	private boolean check() {
		if (!checkModelName()) return false;
		if (!DisplayHelper.check(modelTitleField, this::translate)) return false;
		if (!DisplayHelper.check(modelHintField, this::translate)) return false;
		return DisplayHelper.check(modelDescriptionField, this::translate);
	}

	private boolean checkModelName() {
		if (model.name().equals(modelNameField.value())) return true;
		Language language = box().languageManager().get(model.language());
		return DisplayHelper.checkLanguageName(modelNameField, language, this::translate, box());
	}

	private void saveModel() {
		box().commands(ModelCommands.class).save(model, modelNameField.value(), modelTitleField.value(), modelHintField.value(), modelDescriptionField.value(), username());
		saveAccessType();
		saveCollaborators();
	}

	private void saveAccessType() {
		if (accessType == null) return;
		boolean isPrivate = accessTypeField.state() == ToggleEvent.State.On;
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

	private void removeModel() {
		notifyUser(translate("Removing model..."), UserMessage.Type.Loading);
		box().commands(ModelCommands.class).remove(model, username());
		hideUserNotification();
		notifier.dispatch(PathHelper.languagePath(model.language()));
	}

	private void saveLanguageProperties() {
		String languageName = Language.nameOf(model.name());
		if (!box().languageManager().exists(languageName)) return;
		Language language = box().languageManager().get(languageName);
		box().commands(LanguageCommands.class).saveProperties(language, model.hint(), model.description(), username());
	}

}