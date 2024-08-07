package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.actionable.ToggleEvent;
import io.intino.ime.box.commands.WorkspaceCommands;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.model.Workspace;

import java.util.function.Consumer;

public class WorkspaceSettingsEditor extends AbstractWorkspaceSettingsEditor<ImeBox> {
	private Workspace workspace;
	private Mode mode = Mode.Large;
	private Consumer<String> saveTitleListener;

	public WorkspaceSettingsEditor(ImeBox box) {
		super(box);
	}

	public void workspace(Workspace workspace) {
		this.workspace = workspace;
	}

	public enum Mode { Small, Large }
	public void mode(Mode mode) {
		this.mode = mode;
	}

	public void onSaveTitle(Consumer<String> listener) {
		this.saveTitleListener = listener;
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
		settingsTitleField.value(workspace.title());
		accessTypeField.state(workspace.isPrivate() ? ToggleEvent.State.On : ToggleEvent.State.Off);
		accessTokenField.visible(workspace.isPrivate());
		if (accessTokenField.isVisible()) accessTokenField.value(workspace.token());
		accessTypeField.onToggle(e -> accessTokenField.visible(e.state() == ToggleEvent.State.On));
	}

	private void saveSettings() {
		if (!DisplayHelper.check(settingsTitleField, this::translate)) return;
		boolean isPrivate = accessTypeField.state() == ToggleEvent.State.On;
		settingsDialog.close();
		box().commands(WorkspaceCommands.class).saveTitle(workspace, settingsTitleField.value(), username());
		saveAccessType(isPrivate);
		saveTitleListener.accept(settingsTitleField.value());
	}

	private void saveAccessType(boolean isPrivate) {
		if (isPrivate) box().commands(WorkspaceCommands.class).makePrivate(workspace, accessTokenField.value(), username());
		else box().commands(WorkspaceCommands.class).makePublic(workspace, username());
	}

}