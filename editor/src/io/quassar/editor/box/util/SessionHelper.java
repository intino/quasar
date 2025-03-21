package io.quassar.editor.box.util;

import io.intino.alexandria.ui.services.push.UISession;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.ui.types.LanguageView;
import io.quassar.editor.box.ui.types.LanguagesTab;
import io.quassar.editor.box.ui.types.LanguagesView;

public class SessionHelper {

	public static void register(UISession session, LanguagesTab tab) {
		if (tab == null) return;
		session.add("languages-tab", tab.name());
	}

	public static LanguagesTab languagesTab(UISession session) {
		String result = session.preference("languages-tab");
		return result != null && !result.isEmpty() ? LanguagesTab.from(result) : LanguagesTab.Home;
	}

	public static void register(UISession session, LanguagesView view) {
		if (view == null) return;
		session.add("languages-view", view.name());
	}

	public static LanguagesView languagesView(UISession session) {
		String result = session.preference("languages-view");
		return result != null && !result.isEmpty() ? LanguagesView.from(result) : LanguagesView.PublicLanguages;
	}

	public static void register(UISession session, LanguageTab tab) {
		if (tab == null) return;
		session.add("language-tab", tab.name());
	}

	public static LanguageTab languageTab(UISession session) {
		String result = session.preference("language-tab");
		return result != null && !result.isEmpty() ? LanguageTab.from(result) : LanguageTab.Home;
	}

	public static void register(UISession session, LanguageView view) {
		if (view == null) return;
		session.add("language-view", view.name());
	}

	public static LanguageView languageView(UISession session) {
		String result = session.preference("language-view");
		return result != null && !result.isEmpty() ? LanguageView.from(result) : LanguageView.PublicModels;
	}

}