package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.AddItemEvent;
import io.intino.alexandria.ui.utils.DelayerUtil;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.datasources.WorkspacesDatasource;
import io.intino.ime.box.ui.displays.rows.WorkspaceTableRow;
import io.intino.ime.box.workspaces.Workspace;

import java.util.function.Consumer;

public class WorkspacesTemplate extends AbstractWorkspacesTemplate<ImeBox> {
	private String _title;
	private WorkspacesDatasource source;
	private Consumer<Workspace> openWorkspaceListener;

	public WorkspacesTemplate(ImeBox box) {
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
			DelayerUtil.execute(this, v -> notifier.redirect(session().browser().baseUrl() + "/workspace/" + workspace.name()), 600);
		});
		row.workspaceOwnerItem.owner.value(workspace.owner().fullName());
		row.lastModifiedDateItem.lastModifiedDate.value(workspace.lastModifyDate());
	}

}