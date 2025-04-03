package io.quassar.editor.box.util;

import io.intino.alexandria.logger.Logger;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class LanguageHelper {

	public static final String MavenDslFile = "%s/repository/tara/dsl/%s/%s/%s-%s.jar";
	public static File mavenDslFile(String name, String version, EditorBox box) {
		return new File(MavenDslFile.formatted(box.configuration().languageRepository(), mavenDirectory(name), version, Formatters.normalizeLanguageName(name), version));
	}

	public static Model model(Language language, EditorBox box) {
		Language parentLanguage = box.languageManager().get(language.parent());
		if (parentLanguage == null) return null;
		return box.modelManager().get(parentLanguage, language.name());
	}

	public static URL logo(String language, EditorBox box) {
		return logo(box.languageManager().get(language), box);
	}

	public static URL logo(Language language, EditorBox box) {
		try {
			File result = box.archetype().languages().logo(Language.nameOf(language.name()));
			return result.exists() ? result.toURI().toURL() : ModelHelper.class.getResource("/images/language-logo.png");
		} catch (MalformedURLException e) {
			Logger.error(e);
			return null;
		}
	}

	private static String mavenDirectory(String language) {
		return language.toLowerCase().replace("-", "");
	}

}
