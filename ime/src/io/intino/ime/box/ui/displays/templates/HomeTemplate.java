package io.intino.ime.box.ui.displays.templates;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.datasources.AllWorkspacesDatasource;
import io.intino.ime.box.util.Formatters;
import io.intino.ime.box.workspaces.Workspace;

public class HomeTemplate extends AbstractHomeTemplate<ImeBox> {

	public HomeTemplate(ImeBox box) {
		super(box);
	}

	@Override
	public void init() {
		super.init();
		header.onSearch(this::filter);
		workspacesMagazine.onOpenWorkspace(this::notifyOpeningWorkspace);
		refresh();
	}

	@Override
	public void refresh() {
		super.refresh();
		AllWorkspacesDatasource source = new AllWorkspacesDatasource(box(), session());
		countModels.value(Formatters.countMessage(source.itemCount(), "workspace", "workspaces", language()));
		refreshWorkspaces();
	}

	private void refreshWorkspaces() {
		workspacesMagazine.source(new AllWorkspacesDatasource(box(), session()));
		workspacesMagazine.refresh();
	}

	private void notifyOpeningWorkspace(Workspace workspace) {
		bodyBlock.hide();
		openingWorkspaceMessage.value(String.format(translate("Opening %s"), workspace.title()));
		searchingWorkspacesBlock.show();
	}

	private void filter(String condition) {
		workspacesMagazine.filter(condition);
	}

}