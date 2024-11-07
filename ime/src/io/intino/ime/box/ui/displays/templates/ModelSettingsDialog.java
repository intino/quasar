package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.actionable.ToggleEvent;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.ModelCommands;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.model.Model;

import java.util.function.Consumer;

public class ModelSettingsDialog extends AbstractModelSettingsDialog<ImeBox> {
	private Model model;
	private Mode mode = Mode.Large;
	private View view = View.Default;
	private Consumer<String> saveTitleListener;
	private Consumer<Boolean> saveAccessTypeListener;

	public ModelSettingsDialog(ImeBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
	}

	public enum Mode { Small, Large }
	public void mode(Mode mode) {
		this.mode = mode;
	}

	public enum View { List, Default }
	public void view(View view) {
		this.view = view;
	}

	public void onSaveAccessType(Consumer<Boolean> listener) {
		this.saveAccessTypeListener = listener;
	}

	@Override
	public void init() {
		super.init();
		generalBlock.onInit(e -> initGeneralBlock());
		generalBlock.onShow(e -> refreshGeneralBlock());
		settingsDialog.onOpen(e -> refreshDialog());
		saveSettings.onExecute(e -> saveSettings());
	}

	@Override
	public void refresh() {
		super.refresh();
		largeIcon.visible(mode == Mode.Large && view == View.Default);
		smallIcon.visible(mode == Mode.Small && view == View.Default);
		largeIconInList.visible(mode == Mode.Large && view == View.List);
		smallIconInList.visible(mode == Mode.Small && view == View.List);
	}

	private void refreshDialog() {
		settingsTabSelector.select(0);
	}

	private void initGeneralBlock() {
		removeModel.onExecute(e -> removeModel());
	}

	private void refreshGeneralBlock() {
		settingsTitleField.value(ModelHelper.label(model, language(), box()));
		settingsTitleField.readonly(ModelHelper.isMetamodel(model, box()));
		removeModel.readonly(ModelHelper.canRemove(model, box()));
		accessTypeField.state(model.isPrivate() ? ToggleEvent.State.On : ToggleEvent.State.Off);
	}

	private void saveSettings() {
		if (!DisplayHelper.check(settingsTitleField, this::translate)) return;
		boolean isPrivate = accessTypeField.state() == ToggleEvent.State.On;
		settingsDialog.close();
		saveAccessType(isPrivate);
		saveGitSettings();
		if (saveTitleListener != null) saveTitleListener.accept(settingsTitleField.value());
		if (saveAccessTypeListener != null) saveAccessTypeListener.accept(isPrivate);
	}

	private void saveAccessType(boolean isPrivate) {
		if (isPrivate) box().commands(ModelCommands.class).makePrivate(model, username());
		else box().commands(ModelCommands.class).makePublic(model, username());
	}

	private void saveGitSettings() {
		box().commands(ModelCommands.class).saveGitSettings(model, settingsGitUrlField.value(), settingsGitBranchField.value(), username());
	}

	private void removeModel() {
		box().commands(ModelCommands.class).remove(model, username());
		notifier.redirect(PathHelper.dashboardUrl(session()));
	}

}