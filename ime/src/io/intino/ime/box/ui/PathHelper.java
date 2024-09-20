package io.intino.ime.box.ui;

import io.intino.alexandria.ui.services.push.UISession;
import io.intino.alexandria.ui.services.push.User;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;
import io.intino.ime.model.Release;

public class PathHelper {

	public static final String PublicUser = "public";

	public static String homeUrl(UISession session) {
		return session.browser().baseUrl() + "/" + (session.isLogged() ? "home" : "");
	}

	public static String languagePath(Language language) {
		return languagePath(language.name());
	}

	public static String languagePath(String language) {
		return "/languages/" + language;
	}

	public static String modelsUrl(UISession session) {
		return session.browser().baseUrl() + "/" + userOf(session) + "/models";
	}

	public static String publicModelPath(Model model) {
		if (model == null) return null;
		return "/languages/" + Language.nameOf(model.modelingLanguage()) + "/models/" + model.id();
	}

	public static String modelsPath(Release release) {
		return modelsPath(release.language());
	}

	public static String modelsPath(Language language) {
		return modelsPath(language.name());
	}

	public static String modelsPath(String language) {
		return "/languages/" + language + "/models";
	}

	public static String languagesPath(Language language) {
		return "/languages/" + language.name() + "/languages";
	}

	public static String userHomePath(UISession session) {
		return "/" + userOf(session) + "/home";
	}

	public static String modelPath(Language language, Model model) {
		return "/languages/" + language.name() + "/models/" + model.id();
	}

	public static String modelPath(UISession session, Model model) {
		return "/" + userOf(session) + "/models/" + model.id();
	}

	public static String modelPath(String user, Model model) {
		return "/" + user + "/models/" + model.id();
	}

	public static String modelUrl(UISession session, Model model) {
		return session.browser().baseUrl() + "/" + userOf(session) + "/models/" + model.id();
	}

	private static String userOf(UISession session) {
		User user = session.user();
		return user != null ? user.username() : "public";
	}

}
