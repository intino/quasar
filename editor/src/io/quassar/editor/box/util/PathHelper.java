package io.quassar.editor.box.util;

import io.intino.alexandria.ui.services.push.UISession;
import io.quassar.editor.box.models.File;
import io.quassar.editor.box.ui.types.*;
import io.quassar.editor.model.*;

public class PathHelper {

	public static String homeUrl(UISession session) {
		return session.browser().baseUrl();
	}

	public static String aboutUrl(UISession session) {
		return session.browser().baseUrl() + aboutPath();
	}

	public static String aboutPath() {
		return "/about";
	}

	public static String homePath() {
		return "/";
	}

	public static String commitUrl(Model model, String version, UISession session) {
		return commitUrl(model.release(version), session);
	}

	public static String commitUrl(ModelRelease release, UISession session) {
		return session.browser().baseUrl() + "/commits/%s".formatted(release.commit());
	}

	public static String commitFileFileUrl(Model model, String version, File file, UISession session) {
		ModelRelease release = model.release(version);
		return session.browser().baseUrl() + "/commits/%s/%s".formatted(release.commit(), file.uri());
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

	public static String collectionUrl(Collection collection, UISession session) {
		return session.browser().baseUrl() + "/collections/" + collection.id();
	}

	public static String collectionPath(String address, Collection collection) {
		return address.replace(":collection", collection.id());
	}

	public static String languagesUrl(UISession session) {
		return homeUrl(session);
	}

	public static String languagesPath(String address, LanguagesTab tab) {
		return address + "?tab=" + (tab != null ? tab.name().toLowerCase() : LanguagesTab.PublicLanguages.name().toLowerCase());
	}

	public static String languageUrl(Language language, UISession session) {
		return session.browser().baseUrl() + "/models/" + language.key();
	}

	public static String languageUrl(String language, UISession session) {
		return session.browser().baseUrl() + "/models/" + language;
	}

	public static String languagePath(Language language) {
		return languagePath("/models/:language", language.key(), null);
	}

	public static String languagePath(String language) {
		return languagePath("/models/:language", language, null);
	}

	public static String languagePath(String address, Language language) {
		return languagePath(address, language.key(), null);
	}

	public static String languagePath(String address, String language) {
		return languagePath(address, language, null);
	}

	public static String languagePath(String address, Language language, LanguageTab tab) {
		return languagePath(address, language.key(), tab);
	}

	public static String languagePath(String address, String language, LanguageTab tab) {
		String result = address.replace(":language", language);
		result += tab != null ? (result.contains("?") ? "&" : "?") + "tab=" + tab.name().toLowerCase() : "";
		return result;
	}

	public static String modelUrl(Model model, UISession session) {
		if (model == null) return null;
		return modelUrl(model, null, session);
	}

	public static String modelUrlFromForge(Model model, UISession session) {
		if (model == null) return null;
		return modelUrl(model, null, session) + "?from=forge";
	}

	public static boolean comesFromForge(UISession session) {
		return session.browser().requestUrl().toLowerCase().contains("from=forge");
	}

	public static String modelUrl(Model model, String release, UISession session) {
		return session.browser().baseUrl() + "/models/" + model.language().languageId() + "/" + model.id() + "/" + (release != null ? release : "");
	}

	private static final String ModelPath = "/models/:language/:model/:release";
	public static String modelPath(Model model) {
		return modelPath(ModelPath, model.language().languageId(), model.id(), null, LanguageTab.About.name(), null, null, null);
	}

	public static String startingModelPath(Model model) {
		return modelPath(model);
		//return modelPath(model) + "/starting";
	}

	public static String modelTemplateUrl(Model model, UISession session) {
		return session.browser().baseUrl() + modelPath(model) + "/template";
	}

	public static String modelPath(String path, Model model) {
		return modelPath(path, model.language().languageId(), model.id(), null, null, null, null, null);
	}

	public static String modelPath(Model model, String release) {
		return modelPath(ModelPath, model.language().languageId(), model.id(), release, null, null, null, null);
	}

	public static String modelPath(Model model, String release, LanguageTab tab, ModelView view) {
		String tabName = tab != null ? tab.name() : LanguageTab.About.name();
		String viewName = view != null ? view.name() : "";
		return modelPath(ModelPath, model.language().languageId(), model.id(), release, tabName, viewName, null, null);
	}

	public static String modelPath(Model model, String release, LanguageTab tab, ModelView view, File file) {
		String tabName = tab != null ? tab.name() : LanguageTab.About.name();
		String viewName = view != null ? view.name() : "";
		String fileName = file != null ? file.uri() : "";
		return modelPath(ModelPath, model.language().languageId(), model.id(), release, tabName, viewName, fileName, null);
	}

	public static String modelPath(Model model, String release, LanguageTab tab, ModelView view, String file) {
		String tabName = tab != null ? tab.name() : LanguageTab.About.name();
		String viewName = view != null ? view.name() : "";
		return modelPath(ModelPath, model.language().languageId(), model.id(), release, tabName, viewName, file, null);
	}

	public static String modelPath(String path, Model model, String release, LanguageTab tab, ModelView view, File file, FilePosition position) {
		String tabName = tab != null ? tab.name() : LanguageTab.About.name();
		String viewName = view != null ? view.name() : "";
		String positionValue = position != null ? position.line() + "-" + position.column() : null;
		return modelPath(path, model.language().languageId(), model.id(), release, tabName, viewName, file != null ? file.uri() : null, positionValue);
	}

	public static String modelPath(String path, Model model, String release, LanguageTab tab, ModelView view, String file, FilePosition position) {
		String tabName = tab != null ? tab.name() : LanguageTab.About.name();
		String viewName = view != null ? view.name() : "";
		String positionValue = position != null ? position.line() + "-" + position.column() : null;
		return modelPath(path, model.language().languageId(), model.id(), release, tabName, viewName, file, positionValue);
	}

	public static String modelPath(Model model, String release, LanguageTab tab, File file) {
		String tabName = tab != null ? tab.name() : LanguageTab.About.name();
		String viewName = file != null && file.isResource() ? ModelView.Resources.name() : ModelView.Model.name();
		String fileName = file != null ? file.uri() : "";
		return modelPath(ModelPath, model.language().languageId(), model.id(), release, tabName, viewName, fileName, null);
	}

	public static String modelPath(Model model, String release, LanguageTab tab, File file, FilePosition position) {
		String tabName = tab != null ? tab.name() : LanguageTab.About.name();
		String viewName = file != null && file.isResource() ? ModelView.Resources.name() : ModelView.Model.name();
		String fileName = file != null ? file.uri() : "";
		String positionValue = position != null ? position.line() + "-" + position.column() : null;
		return modelPath(ModelPath, model.language().languageId(), model.id(), release, tabName, viewName, fileName, positionValue);
	}

	public static String modelUrl(Model model, String release, LanguageTab tab, ModelView view, io.quassar.editor.box.models.File file, FilePosition position, UISession session) {
		String tabName = tab != null ? tab.name() : null;
		String viewName = view != null ? view.name() : null;
		String fileUri = file != null ? file.uri() : null;
		String positionValue = position != null ? position.line() + "-" + position.column() : null;
		return modelUrl(model.language().languageId(), model.id(), release, tabName, viewName, fileUri, positionValue, session);
	}

	public static String modelUrl(String language, String model, String release, String tab, String view, String file, String position, UISession session) {
		return modelPath(session.browser().baseUrl() + ModelPath, language, model, release, tab, view, file, position);
	}

	public static String modelPath(String address, String language, String model, String release, String tab, String view, String file, String position) {
		String result = address.replace(":language", language).replace(":model", model).replace(":release", release != null ? release : "");
		result += tab != null ? ((result.contains("?") ? "&" : "?") + "tab=" + tab.toLowerCase()) : "";
		result += view != null ? ((result.contains("?") ? "&" : "?") + "view=" + view.toLowerCase()) : "";
		result += file != null ? ((result.contains("?") ? "&" : "?") + "file=" + file) : "";
		result += position != null ? ((result.contains("?") ? "&" : "?") + "pos=" + position) : "";
		return result;
	}

	public static String modelViewPath(String address, Model model, String release, LanguageTab tab) {
		String result = address.replace(":language", model.language().languageId()).replace(":model", model.id()).replace(":release", release);
		result += tab != null ? "?tab=" + tab.name() : "";
		result += (result.contains("?") ? "&" : "?") + "view=:view";
		return result;
	}

	public static String languageReleaseHelp(String address, Language language, LanguageRelease release) {
		return languageReleaseHelp(address, language, release.version());
	}

	public static String languageReleaseHelp(String address, Language language, String release) {
		return address.replace(":language", language.key()) + "?version=" + release;
	}

	private static final String LanguageReleaseHelpPath = "/models/:language/help";
	public static String languageReleaseHelp(Language language, LanguageRelease release) {
		return languageReleaseHelp(LanguageReleaseHelpPath, language, release);
	}

	public static String languageReleaseHelp(Language language, String release) {
		return languageReleaseHelp(LanguageReleaseHelpPath, language, release);
	}

	private static final String ForgePath = "/forge/:model/:release";
	public static String forgeUrl(Model model, String release, UISession session) {
		String lastRelease = model.lastRelease() != null ? model.lastRelease().version() : "1.0.0";
		return session.browser().baseUrl() + ForgePath.replace(":model", model.id()).replace(":release", release != null && !release.equals(Model.DraftRelease) ? release : lastRelease);
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
