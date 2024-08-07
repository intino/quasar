package io.intino.ime.box.ui.displays.templates;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.WorkspaceCommands;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.model.Workspace;

public class WorkspaceHeaderTemplate extends AbstractWorkspaceHeaderTemplate<ImeBox> {
	private Workspace workspace;
	private String _title;
	private String _description = null;

	public WorkspaceHeaderTemplate(ImeBox box) {
		super(box);
	}

	public void workspace(Workspace workspace) {
		this.workspace = workspace;
	}

	public void title(String title) {
		this._title = title;
	}

	public void description(String value) {
		this._description = value;
	}

	@Override
	public void init() {
		super.init();
		cancel.onExecute(e -> hideTitleEditor());
		save.onExecute(e -> saveTitle());
		titleField.onEnterPress(e -> saveTitle());
		workspaces.visible(session().user() != null);
		user.visible(session().user() != null);
		settingsEditor.onSaveTitle(this::updateTitle);
	}

	@Override
	public void refresh() {
		super.refresh();
		titleLink.title(_title);
		titleLink.onExecute(e -> showTitleEditor());
		description.value(_description);
		if (session().user() == null) return;
		workspaces.path(PathHelper.workspacesPath(session()));
		myWorkspaces.path(PathHelper.workspacesPath(session()));
		settingsEditor.workspace(workspace);
		settingsEditor.mode(WorkspaceSettingsEditor.Mode.Large);
		settingsEditor.refresh();
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
		box().commands(WorkspaceCommands.class).saveTitle(workspace, _title, username());
		hideTitleEditor();
		refresh();
	}

	private void updateTitle(String title) {
		_title = title;
		refresh();
	}

}