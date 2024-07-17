package io.intino.languageeditor.box.ui.datasources;

import io.intino.alexandria.ui.services.push.UISession;
import io.intino.languageeditor.box.LanguageEditorBox;
import io.intino.languageeditor.box.workspaces.Workspace;

import java.util.List;

public class SharedWorkspacesDatasource extends WorkspacesDatasource {

	public SharedWorkspacesDatasource(LanguageEditorBox box, UISession session) {
		super(box, session);
	}

	@Override
	protected List<Workspace> load() {
		return box.workspaceManager().sharedWorkspaces(username());
	}

}
