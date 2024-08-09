package io.intino.ime.box.ui.displays.templates;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.model.Workspace;

public class NotFoundTemplate extends AbstractNotFoundTemplate<ImeBox> {
	private Workspace workspace;

	public NotFoundTemplate(ImeBox box) {
		super(box);
	}

	public void workspace(String name) {
		this.workspace = box().workspaceManager().workspace(name);
	}

	@Override
	public void refresh() {
		super.refresh();
		message.value(translate("Workspace was not found"));
		myWorkspaces.visible(session().user() != null);
		myWorkspaces.path(PathHelper.workspacesPath(session()));
	}

}