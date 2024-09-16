package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.actionable.ToggleEvent;
import io.intino.ime.box.commands.ModelCommands;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.model.Model;

import java.util.function.Consumer;

public class ModelSettingsEditor extends AbstractModelSettingsEditor<ImeBox> {
	private Model model;
	private Mode mode = Mode.Large;
	private Consumer<String> saveTitleListener;
	private Consumer<Boolean> saveAccessTypeListener;

	public ModelSettingsEditor(ImeBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
	}

	public enum Mode { Small, Large }
	public void mode(Mode mode) {
		this.mode = mode;
	}

	public void onSaveTitle(Consumer<String> listener) {
		this.saveTitleListener = listener;
	}

	public void onSaveAccessType(Consumer<Boolean> listener) {
		this.saveAccessTypeListener = listener;
	}

	@Override
	public void init() {
		super.init();
		settingsDialog.onOpen(e -> refreshDialog());
		saveSettings.onExecute(e -> saveSettings());
	}

	@Override
	public void refresh() {
		super.refresh();
		largeIcon.visible(mode == Mode.Large);
		smallIcon.visible(mode == Mode.Small);
	}

	public void refreshDialog() {
		settingsTitleField.value(model.title());
		accessTypeField.state(model.isPrivate() ? ToggleEvent.State.On : ToggleEvent.State.Off);
		accessTokenField.visible(model.isPrivate());
		if (accessTokenField.isVisible()) accessTokenField.value(model.token());
		accessTypeField.onToggle(e -> accessTokenField.visible(e.state() == ToggleEvent.State.On));
		versionEditor.model(model);
		versionEditor.refresh();
	}

	private void saveSettings() {
		if (!DisplayHelper.check(settingsTitleField, this::translate)) return;
		if (!versionEditor.check()) return;
		boolean isPrivate = accessTypeField.state() == ToggleEvent.State.On;
		settingsDialog.close();
		box().commands(ModelCommands.class).saveTitle(model, settingsTitleField.value(), username());
		saveAccessType(isPrivate);
		box().commands(ModelCommands.class).saveVersion(model, versionEditor.version(), username());
		if (saveTitleListener != null) saveTitleListener.accept(settingsTitleField.value());
		if (saveAccessTypeListener != null) saveAccessTypeListener.accept(isPrivate);
	}

	private void saveAccessType(boolean isPrivate) {
		if (isPrivate) box().commands(ModelCommands.class).makePrivate(model, accessTokenField.value(), username());
		else box().commands(ModelCommands.class).makePublic(model, username());
	}

}