package io.quassar.editor.box.util;

import io.intino.alexandria.ui.services.push.UISession;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.models.File;
import io.quassar.editor.box.ui.types.*;
import io.quassar.editor.model.FilePosition;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import io.quassar.editor.model.Model;

public class PathHelper {

	public static String homeUrl(UISession session) {
		return session.browser().baseUrl();
	}

	public static String aboutUrl(UISession session) {
		return session.browser().baseUrl() + "/about";
	}

	public static String homePath() {
		return "/";
	}

	public static String fileUrl(Model model, String release, File file, UISession session, EditorBox box) {
		return session.browser().baseUrl() + "/models/%s/download/file".formatted(model.id()) + "?release=%s&file=%s".formatted(release, file.uri());
	}

	public static String landingUrl(LandingDialog dialog, UISession session) {
		return homeUrl(session) + "?dialog=" + dialog.name().toLowerCase();
	}

	public static String landingPath(String path, LandingDialog dialog) {
		return path + (dialog != null ? "?dialog=" + dialog.name().toLowerCase() : "");
	}

	public static String permissionsUrl(Language language, String callbackUrl, UISession session) {
		return session.browser().baseUrl() + "/permissions?language=" + language.key() + "&callback=" + callbackUrl;
	}

	public static String permissionsUrl(Model model, String callbackUrl, UISession session) {
		return session.browser().baseUrl() + "/permissions?model=" + model.id() + "&callback=" + callbackUrl;
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
		return languagePath("/languages/:language", language.key(), null, null);
	}

	public static String languagePath(String language) {
		return languagePath("/languages/:language", language, null, null);
	}

	public static String languagePath(String address, Language language) {
		return languagePath(address, language.key(), null, null);
	}

	public static String languagePath(String address, String language) {
		return languagePath(address, language, null, null);
	}

	public static String languagePath(String address, String language, LanguageTab tab, LanguageView view) {
		String result = address.replace(":language", language);
		result += tab != null ? (result.contains("?") ? "&" : "?") + "tab=" + tab.name().toLowerCase() : "";
		result += view != null ? (result.contains("?") ? "&" : "?") + "view=" + view.name().toLowerCase() : "";
		return result;
	}

	public static String languagePath(String address, Language language, LanguageTab tab) {
		return languagePath(address, language.key(), tab, null);
	}

	public static String languagePath(String address, Language language, LanguageTab tab, LanguageView view) {
		return languagePath(address, language.key(), tab, view);
	}

	public static String modelUrl(Model model, UISession session) {
		return modelUrl(model, null, session);
	}

	public static String modelUrl(Model model, String release, UISession session) {
		return session.browser().baseUrl() + "/models/" + model.id() + (release != null ? "?release=" + release : "");
	}

	private static final String ModelPath = "/models/:model";
	public static String modelPath(Model model) {
		return modelPath(ModelPath, model, null, null, null, null);
	}

	public static String startingModelPath(Model model) {
		return modelPath(model) + "/starting";
	}

	public static String modelTemplateUrl(Model model, UISession session) {
		return session.browser().baseUrl() + modelPath(model) + "/template";
	}

	public static String modelPath(Model model, String release) {
		return modelPath(ModelPath, model, release, null, null, null);
	}

	public static String modelPath(Model model, String release, File file) {
		return modelPath(ModelPath, model, release, null, file != null ? file.uri() : null, null);
	}

	public static String modelPath(String address, Model model) {
		return modelPath(address, model, null, null, null, null);
	}

	public static String modelPath(String address, Model model, String release, String file, FilePosition position) {
		ModelView view = file != null && io.quassar.editor.box.models.File.isResource(file) ? ModelView.Resources : ModelView.Model;
		return modelPath(address, model, release, view, file, position);
	}

	public static String modelUrl(String model, String release, String view, String file, String position, UISession session) {
		return modelPath(session.browser().baseUrl() + "/models/:model", model, release, view, file, position);
	}

	public static String modelPath(String address, Model model, String release, ModelView view, String file, FilePosition position) {
		return modelPath(address, model.id(), release, view != null ? view.name() : null, file, position != null ? position.line() + "-" + position.column() : null);
	}

	public static String modelPath(String address, String model, String release, String view, String file, String position) {
		String result = address.replace(":model", model);
		result += release != null ? "?release=" + release : "";
		result += view != null ? ((result.contains("?") ? "&" : "?") + "view=" + view) : "";
		result += file != null ? ((result.contains("?") ? "&" : "?") + "file=" + file) : "";
		result += position != null ? ((result.contains("?") ? "&" : "?") + "pos=" + position) : "";
		return result;
	}

	public static String modelViewPath(String address, Model model, String release) {
		String result = address.replace(":model", model.id());
		result += release != null ? "?release=" + release : "";
		result += (result.contains("?") ? "&" : "?") + "view=:view";
		return result;
	}

	public static String languageReleaseHelp(String address, Language language, LanguageRelease release) {
		return languageReleaseHelp(address, language, release.version());
	}

	public static String languageReleaseHelp(String address, Language language, String release) {
		return address.replace(":language", language.key()) + "?version=" + release;
	}

	private static final String LanguageReleaseHelpPath = "/languages/:language/help";
	public static String languageReleaseHelp(Language language, LanguageRelease release) {
		return languageReleaseHelp(LanguageReleaseHelpPath, language, release);
	}

	public static String languageReleaseHelp(Language language, String release) {
		return languageReleaseHelp(LanguageReleaseHelpPath, language, release);
	}

	private static final String ForgePath = "/forge/:model/:release";
	public static String forgeUrl(Model model, String release, UISession session) {
		return session.browser().baseUrl() + ForgePath.replace(":model", model.id()).replace(":release", release);
	}

	public static String forgePath(String address, String model, String release) {
		String result = address.replace(":model", model).replace(":release", release);
		result += "?" + "view=:view";
		return result;
	}

	public static String forgeReleasePath(String address, String model, String view) {
		String result = address.replace(":model", model);
		result += "?" + "view=%s".formatted(view);
		return result;
	}

}
