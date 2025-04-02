package io.quassar.editor.box.util;

import io.intino.alexandria.ui.services.push.UISession;
import io.quassar.editor.box.models.ModelContainer;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.ui.types.LanguagesTab;
import io.quassar.editor.box.ui.types.ModelView;
import io.quassar.editor.model.FilePosition;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

public class PathHelper {

	public static String homeUrl(UISession session) {
		return session.browser().baseUrl();
	}

	public static String permissionsUrl(Language language, String callbackUrl, UISession session) {
		return session.browser().baseUrl() + "/permissions?language=" + language.name() + "&callback=" + callbackUrl;
	}

	public static String permissionsUrl(Model model, String callbackUrl, UISession session) {
		return session.browser().baseUrl() + "/permissions?language=" + Language.nameOf(model.language()) + "&model=" + model.name() + "&callback=" + callbackUrl;
	}

	public static String notFoundUrl(String type, UISession session) {
		return session.browser().baseUrl() + "/not-found?type=" + type;
	}

	public static String loginUrl(UISession session) {
		return session.browser().baseUrl() + "/login";
	}

	public static String languagesUrl(UISession session) {
		return homeUrl(session);
	}

	public static String languagesPath(String address, LanguagesTab tab) {
		return address + "?tab=" + (tab != null ? tab.name().toLowerCase() : LanguagesTab.PublicLanguages.name().toLowerCase());
	}

	public static String languageUrl(Language language, UISession session) {
		return session.browser().baseUrl() + "/languages/" + language.name();
	}

	public static String languageUrl(String language, UISession session) {
		return session.browser().baseUrl() + "/languages/" + language;
	}

	public static String languagePath(Language language) {
		return languagePath("/languages/:language", language, null);
	}

	public static String languagePath(String language) {
		return languagePath("/languages/:language", language, null);
	}

	public static String languagePath(String address, Language language) {
		return languagePath(address, language, null);
	}

	public static String languagePath(String address, Language language, LanguageTab tab) {
		return address.replace(":language", language.name()) + (tab != null ? "?tab=" + tab.name().toLowerCase() : "");
	}

	public static String languagePath(String address, String language, LanguageTab tab) {
		return address.replace(":language", language) + (tab != null ? "?tab=" + tab.name().toLowerCase() : "");
	}

	public static String modelUrl(Model model, UISession session) {
		return session.browser().baseUrl() + "/languages/" + Language.nameOf(model.language()) + "/models/" + model.name();
	}

	private static final String ModelPath = "/languages/:language/models/:model";
	public static String modelPath(Model model) {
		return modelPath(ModelPath, model, null, null, null, null);
	}

	public static String modelPath(Model model, String release) {
		return modelPath(ModelPath, model, release, null, null, null);
	}

	public static String modelPath(Model model, String release, ModelContainer.File file) {
		return modelPath(ModelPath, model, release, null, file != null ? file.uri() : null, null);
	}

	public static String modelPath(String address, Model model) {
		return modelPath(address, model, null, null, null, null);
	}

	public static String modelPath(String address, Model model, String release, String file, FilePosition position) {
		ModelView view = file != null && Model.isResource(file) ? ModelView.Resources : ModelView.Model;
		return modelPath(address, model, release, view, file, position);
	}

	public static String modelPath(String address, Model model, String release, ModelView view, String file, FilePosition position) {
		String result = address.replace(":language", Language.nameOf(model.language())).replace(":model", model.name());
		result += release != null ? "?release=" + release : "";
		result += view != null ? ((result.contains("?") ? "&" : "?") + "view=" + view.name()) : "";
		result += file != null ? ((result.contains("?") ? "&" : "?") + "file=" + file) : "";
		result += position != null ? ((result.contains("?") ? "&" : "?") + "pos=" +position.line() + "-" + position.column()) : "";
		return result;
	}

	public static String modelViewPath(String address, Model model, String release) {
		String result = address.replace(":language", Language.nameOf(model.language())).replace(":model", model.name());
		result += release != null ? "?release=" + release : "";
		result += (result.contains("?") ? "&" : "?") + "view=:view";
		return result;
	}

}
