package io.quassar.editor.box.util;

import io.intino.alexandria.ui.services.push.UISession;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.ui.types.LanguagesTab;

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
		String result = session.preference("language-tab");
		return result != null && !result.isEmpty() ? LanguageTab.from(result) : LanguageTab.PublicModels;
	}

}