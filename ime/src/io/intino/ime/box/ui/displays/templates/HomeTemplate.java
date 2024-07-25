package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.SelectionEvent;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.datasources.AllWorkspacesDatasource;
import io.intino.ime.box.ui.datasources.OwnerWorkspacesDatasource;
import io.intino.ime.box.ui.datasources.SharedWorkspacesDatasource;
import io.intino.ime.box.ui.datasources.WorkspacesDatasource;
import io.intino.ime.box.workspaces.Workspace;

public class HomeTemplate extends AbstractHomeTemplate<ImeBox> {

	public HomeTemplate(ImeBox box) {
		super(box);
	}

	@Override
	public void init() {
		super.init();
		workspacesGroupSelector.onSelect(this::selectWorkspacesGroup);
		workspacesGroupSelector.select("allWorkspacesOption");
		workspacesCatalog.onOpenWorkspace(this::notifyOpeningWorkspace);
	}

	private void selectWorkspacesGroup(SelectionEvent event) {
		String selected = event.selection().isEmpty() ? "allWorkspacesOption" : (String) event.selection().getFirst();
		if (selected.equals("sharedWorkspacesOption")) refreshWorkspaces("Shared with you", new SharedWorkspacesDatasource(box(), session()));
		else if (selected.equals("ownerWorkspacesOption")) refreshWorkspaces("Your workspaces", new OwnerWorkspacesDatasource(box(), session()));
		else refreshWorkspaces("All workspaces", new AllWorkspacesDatasource(box(), session()));
	}

	private void refreshWorkspaces(String title, WorkspacesDatasource source) {
		workspacesCatalog.title(title);
		workspacesCatalog.source(source);
		workspacesCatalog.refresh();
	}

	private void notifyOpeningWorkspace(Workspace workspace) {
		bodyBlock.hide();
		openingWorkspaceMessage.value(String.format(translate("Opening %s"), workspace.title()));
		openingWorkspaceBlock.show();
	}

}