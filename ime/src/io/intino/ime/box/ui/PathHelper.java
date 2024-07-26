package io.intino.ime.box.ui;

import io.intino.alexandria.ui.services.push.UISession;
import io.intino.ime.box.workspaces.Workspace;

public class PathHelper {

	public static String workspacePath(UISession session, Workspace workspace) {
		return session.browser().baseUrl() + "/workspaces/" + workspace.name();
	}

}
