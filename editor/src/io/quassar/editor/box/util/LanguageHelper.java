package io.quassar.editor.box.util;

import io.intino.alexandria.logger.Logger;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.model.Language;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class LanguageHelper {

	public static URL logo(String language, EditorBox box) {
		return logo(box.languageManager().get(language), box);
	}

	public static URL logo(Language language, EditorBox box) {
		try {
			File result = box.archetype().languages().logo(Language.nameOf(language.name()));
			return result.exists() ? result.toURI().toURL() : ModelHelper.class.getResource("/language-logo.png");
		} catch (MalformedURLException e) {
			Logger.error(e);
			return null;
		}
	}

}
