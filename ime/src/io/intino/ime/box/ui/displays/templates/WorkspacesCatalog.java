package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.AddItemEvent;
import io.intino.alexandria.ui.utils.DelayerUtil;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.WorkspaceCommands;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.ui.datasources.WorkspacesDatasource;
import io.intino.ime.box.ui.displays.rows.WorkspaceTableRow;
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
	}

	@Override
	public void refresh() {
		super.refresh();
		title.value(translate(_title));
		workspaceTable.source(source);
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
		row.operationsItem.cloneWorkspaceEditor.workspace(workspace);
		row.operationsItem.cloneWorkspaceEditor.mode(CloneWorkspaceEditor.Mode.Small);
		row.operationsItem.cloneWorkspaceEditor.onClone(e -> refresh());
		row.operationsItem.cloneWorkspaceEditor.refresh();
		row.operationsItem.settingsEditor.workspace(workspace);
		row.operationsItem.settingsEditor.onSaveTitle(title -> row.workspaceTitleItem.title.title(title));
		row.operationsItem.settingsEditor.mode(WorkspaceSettingsEditor.Mode.Small);
		row.operationsItem.settingsEditor.refresh();
		row.operationsItem.removeWorkspace.onExecute(e -> remove(workspace));
	}

	private void remove(Workspace workspace) {
		box().commands(WorkspaceCommands.class).remove(workspace, username());
		refresh();
	}

}