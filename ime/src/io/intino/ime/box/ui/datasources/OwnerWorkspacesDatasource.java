package io.intino.ime.box.ui.datasources;

import io.intino.alexandria.ui.services.push.UISession;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.workspaces.Workspace;

import java.util.List;

public class OwnerWorkspacesDatasource extends WorkspacesDatasource {

	public OwnerWorkspacesDatasource(ImeBox box, UISession session) {
		super(box, session);
	}

	@Override
	protected List<Workspace> load() {
		return box.workspaceManager().ownerWorkspaces(username());
	}

}
