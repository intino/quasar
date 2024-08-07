package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.AddItemEvent;
import io.intino.alexandria.ui.utils.DelayerUtil;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.WorkspaceCommands;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.ui.datasources.WorkspacesDatasource;
import io.intino.ime.box.ui.displays.rows.WorkspaceTableRow;
import io.intino.ime.box.util.WorkspaceHelper;
import io.intino.ime.model.Workspace;

import java.util.function.Consumer;

public class WorkspacesCatalog extends AbstractWorkspacesCatalog<ImeBox> {
	private String _title;
	private WorkspacesDatasource source;
	private Consumer<Workspace> openWorkspaceListener;
	private Workspace selectedWorkspace;

	public WorkspacesCatalog(ImeBox box) {
		super(box);
	}

	public void title(String title) {
		this._title = title;
	}

	public void source(WorkspacesDatasource source) {
		this.source = source;
	}

	public void onOpenWorkspace(Consumer<Workspace> listener) {
		this.openWorkspaceListener = listener;
	}

	@Override
	public void init() {
		super.init();
		workspaceTable.onAddItem(this::refresh);
		initWorkspaceDialog();
	}

	@Override
	public void refresh() {
		super.refresh();
		title.value(translate(_title));
		workspaceTable.source(source);
	}

	private void initWorkspaceDialog() {
		workspaceDialog.onOpen(e -> refreshWorkspaceDialog());
		cloneWorkspace.onExecute(e -> cloneWorkspace());
		workspaceNameField.onChange(e -> DisplayHelper.checkWorkspaceName(workspaceNameField, this::translate, box()));
	}

	private void refresh(AddItemEvent event) {
		Workspace workspace = event.item();
		WorkspaceTableRow row = event.component();
		row.workspaceTitleItem.title.title(workspace.title());
		row.workspaceTitleItem.title.onExecute(e -> {
			openWorkspaceListener.accept(workspace);
			DelayerUtil.execute(this, v -> notifier.redirect(PathHelper.workspaceUrl(session(), workspace)), 600);
		});
		row.workspaceOwnerItem.owner.value(workspace.owner().fullName());
		row.lastModifiedDateItem.lastModifiedDate.value(workspace.lastModifyDate());
		row.operationsItem.cloneWorkspaceTrigger.bindTo(workspaceDialog);
		row.operationsItem.cloneWorkspaceTrigger.onOpen(e -> refreshWorkspaceDialog(workspace));
		row.operationsItem.settingsEditor.workspace(workspace);
		row.operationsItem.settingsEditor.onSaveTitle(title -> row.workspaceTitleItem.title.title(title));
		row.operationsItem.settingsEditor.mode(WorkspaceSettingsEditor.Mode.Small);
		row.operationsItem.settingsEditor.refresh();
		row.operationsItem.removeWorkspace.onExecute(e -> remove(workspace));
	}

	private void refreshWorkspaceDialog(Workspace workspace) {
		this.selectedWorkspace = workspace;
		refreshWorkspaceDialog();
	}

	private void refreshWorkspaceDialog() {
		if (selectedWorkspace == null) return;
		workspaceDialog.title(String.format(translate("Clone %s"), selectedWorkspace.title()));
		workspaceNameField.value(WorkspaceHelper.proposeName());
		workspaceTitleField.value(String.format(translate("%s Copy"), selectedWorkspace.title()));
	}

	private void cloneWorkspace() {
		if (!DisplayHelper.checkWorkspaceName(workspaceNameField, this::translate, box())) return;
		if (!DisplayHelper.check(workspaceTitleField, this::translate)) return;
		workspaceDialog.close();
		box().commands(WorkspaceCommands.class).clone(selectedWorkspace, workspaceNameField.value(), workspaceTitleField.value(), username());
		refresh();
	}

	private void remove(Workspace workspace) {
		box().commands(WorkspaceCommands.class).remove(workspace, username());
		refresh();
	}

}