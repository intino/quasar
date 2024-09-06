package io.intino.ime.box.ui;

import io.intino.alexandria.ui.services.push.UISession;
import io.intino.alexandria.ui.services.push.User;
import io.intino.ime.model.Model;

public class PathHelper {

	public static final String PublicUser = "public";

	public static String homeUrl(UISession session) {
		return session.browser().baseUrl() + "/" + (session.isLogged() ? "home" : "");
	}

	public static String modelsUrl(UISession session) {
		return session.browser().baseUrl() + "/" + userOf(session) + "/models";
	}

	public static String modelsPath(UISession session) {
		return "/" + userOf(session) + "/models";
	}

	public static String modelPath(UISession session, Model model) {
		return "/" + userOf(session) + "/models/" + model.name();
	}

	public static String modelUrl(UISession session, Model model) {
		return session.browser().baseUrl() + "/" + userOf(session) + "/models/" + model.name();
	}

	private static String userOf(UISession session) {
		User user = session.user();
		return user != null ? user.username() : "public";
	}
}
