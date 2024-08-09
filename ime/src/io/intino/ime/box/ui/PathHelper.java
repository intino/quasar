package io.intino.ime.box.ui;

import io.intino.alexandria.ui.services.push.UISession;
import io.intino.alexandria.ui.services.push.User;
import io.intino.ime.model.Workspace;

public class PathHelper {

	public static final String PublicUser = "public";

	public static String homeUrl(UISession session) {
		return session.browser().baseUrl() + "/" + (session.isLogged() ? "home" : "");
	}

	public static String workspacesUrl(UISession session) {
		return session.browser().baseUrl() + "/" + userOf(session) + "/workspaces";
	}

	public static String workspacesPath(UISession session) {
		return "/" + userOf(session) + "/workspaces";
	}

	public static String workspacePath(UISession session, Workspace workspace) {
		return "/" + userOf(session) + "/workspaces/" + workspace.name();
	}

	public static String workspaceUrl(UISession session, Workspace workspace) {
		return session.browser().baseUrl() + "/" + userOf(session) + "/workspaces/" + workspace.name();
	}

	private static String userOf(UISession session) {
		User user = session.user();
		return user != null ? user.username() : "public";
	}
}
