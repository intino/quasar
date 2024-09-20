package io.intino.ime.box.util;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.services.push.User;
import io.intino.ime.box.ImeBox;
import io.intino.ime.model.Language;
import io.intino.ime.model.LanguageLevel;
import io.intino.ime.model.Release;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Function;

public class LanguageHelper {

	public static String title(Language language) {
		return language.name() + " language";
	}

	public static String type(Release release, Function<String, String> translator) {
		return translator.apply(release.level() == LanguageLevel.L1 ? "Model" : "Language");
	}

	public static String styleFormat(Release release) {
		return release.level() == LanguageLevel.L1 ? "greenBackground" : "orangeBackground";
	}

	public static URL logo(String language, ImeBox box) {
		return logo(box.languageManager().get(language), box);
	}

	public static URL logo(Language language, ImeBox box) {
		try {
			File result = box.archetype().repository().logos().getLogo(language.name());
			return result.exists() ? result.toURI().toURL() : ModelHelper.class.getResource("/language-logo.png");
		} catch (MalformedURLException e) {
			Logger.error(e);
			return null;
		}
	}

	public static boolean canCreateLanguage(Language language, Release release, User user) {
		if (user == null) return false;
		if (!language.isPublic() && !language.owner().equals(user.username())) return false;
		if (release == null) return false;
		return release.level() != LanguageLevel.L1;
	}

	public static boolean canViewExampleModels(Language language, Release lastRelease) {
		return lastRelease == null || lastRelease.level() == LanguageLevel.L1;
	}

	public static boolean canViewExampleLanguages(Language language, Release lastRelease) {
		return lastRelease != null && lastRelease.level() != LanguageLevel.L1;
	}

	public static boolean canEdit(Language language, Release release, User user) {
		if (user == null) return false;
		return language.owner().equals(user.username());
	}

	public static boolean canEditOperations(Language language, Release release) {
		return release != null && release.level() != LanguageLevel.L1 && !language.isFoundational();
	}

}
