package io.intino.languageeditor.box.ui.datasources;

import io.intino.alexandria.ui.services.push.UISession;
import io.intino.languageeditor.box.LanguageEditorBox;
import io.intino.languageeditor.box.workspaces.Workspace;

import java.util.List;

public class OwnerWorkspacesDatasource extends WorkspacesDatasource {

	public OwnerWorkspacesDatasource(LanguageEditorBox box, UISession session) {
		super(box, session);
	}

	@Override
	protected List<Workspace> load() {
		return box.workspaceManager().ownerWorkspaces(username());
	}

}
