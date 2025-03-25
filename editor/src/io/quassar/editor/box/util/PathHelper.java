package io.quassar.editor.box.util;

import io.intino.alexandria.ui.services.push.UISession;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.ui.types.LanguageView;
import io.quassar.editor.box.ui.types.LanguagesTab;
import io.quassar.editor.box.ui.types.LanguagesView;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

public class PathHelper {

	public static String homeUrl(UISession session) {
		return session.browser().baseUrl();
	}

	public static String loginUrl(UISession session) {
		return session.browser().baseUrl() + "/login";
	}

	public static String languagesUrl(UISession session) {
		return homeUrl(session);
	}

	public static String languagesPath(String address, LanguagesTab tab) {
		return address + "?tab=" + (tab != null ? tab.name().toLowerCase() : LanguagesTab.Languages.name().toLowerCase());
	}

	public static String languagesPath(String address, LanguagesTab tab, LanguagesView view) {
		String result = address;
		result += "?tab=" + (tab != null ? tab.name().toLowerCase() : LanguagesTab.Languages.name().toLowerCase());
		result += (view != null ? "&view=" + view.name().toLowerCase() : "");
		return result;
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

	public static String languagePath(String address, Language language, LanguageTab tab, LanguageView view) {
		String result = address.replace(":language", language.name());
		result += "?tab=" + (tab != null ? tab.name().toLowerCase() : LanguageTab.Models.name().toLowerCase());
		result += (view != null ? "&view=" + view.name().toLowerCase() : "");
		return result;
	}

	public static String modelUrl(Model model, UISession session) {
		return session.browser().baseUrl() + "/languages/" + Language.nameOf(model.language()) + "/models/" + model.name();
	}

	public static String modelPath(String address, Model model, String version) {
		String result = address.replace(":language", Language.nameOf(model.language())).replace(":model", model.name());
		result += version != null ? "?version=" + version : "";
		return result;
	}

	public static String modelPath(String address, Model model) {
		return modelPath(address, model, null);
	}

	public static String modelPath(Model model) {
		return modelPath(model, null);
	}

	public static String modelPath(Model model, String version) {
		return modelPath("/languages/:language/models/:model", model, version);
	}
}
