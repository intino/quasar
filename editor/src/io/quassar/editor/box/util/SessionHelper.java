package io.quassar.editor.box.util;

import io.intino.alexandria.ui.services.push.UISession;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.ui.types.LanguageView;
import io.quassar.editor.box.ui.types.LanguagesTab;
import io.quassar.editor.box.ui.types.ModelView;

public class SessionHelper {

	public static void register(UISession session, LanguagesTab tab) {
		if (tab == null) return;
		session.add("languages-tab", tab.name());
	}

	public static LanguagesTab languagesTab(UISession session) {
		String result = session.preference("languages-tab");
		return result != null && !result.isEmpty() ? LanguagesTab.from(result) : LanguagesTab.PublicLanguages;
	}

	public static void register(UISession session, LanguageTab tab) {
		if (tab == null) return;
		session.add("language-tab", tab.name());
	}

	public static LanguageTab languageTab(UISession session) {
		if (session.user() == null) return LanguageTab.Examples;
		String result = session.preference("language-tab");
		return result != null && !result.isEmpty() ? LanguageTab.from(result) : LanguageTab.Models;
	}

	public static void register(UISession session, LanguageView view) {
		if (view == null) return;
		session.add("language-view", view.name());
	}

	public static LanguageView languageView(UISession session) {
		String result = session.preference("language-view");
		return result != null && !result.isEmpty() ? LanguageView.from(result) : LanguageView.About;
	}

	public static void register(UISession session, ModelView view) {
		if (view == null) return;
		session.add("model-view", view.name());
	}

	public static ModelView modelView(UISession session) {
		String result = session.preference("model-view");
		return result != null && !result.isEmpty() ? ModelView.from(result) : ModelView.Model;
	}

}