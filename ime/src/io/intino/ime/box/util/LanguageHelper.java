package io.intino.ime.box.util;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.services.push.User;
import io.intino.ime.box.ImeBox;
import io.intino.ime.model.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.function.Function;

public class LanguageHelper {

	public static String label(Language language, Function<String, String> translator) {
		return String.format(translator.apply("%s language"), language.name());
	}

	public static String shortLabel(Language language, Function<String, String> translator) {
		return language.name();
	}

	public static LanguageLevel level(Language language, ImeBox box) {
		Release release = box.languageManager().lastRelease(language.name());
		if (release == null) return LanguageLevel.L1;
		return release.level();
	}

	public static Language l3Parent(Language language, ImeBox box) {
		if (level(language, box) == LanguageLevel.L3) return null;
		return parent(language, LanguageLevel.L3, box);
	}

	public static Language l2Parent(Language language, ImeBox box) {
		if (level(language, box) == LanguageLevel.L3) return null;
		if (level(language, box) == LanguageLevel.L2) return null;
		return parent(language, LanguageLevel.L2, box);
	}

	public static Language parent(Language language, LanguageLevel parentLevel, ImeBox box) {
		Language result = parent(language, box);
		if (result == null) return null;
		LanguageLevel level = level(result, box);
		while (level != parentLevel) {
			result = parent(result, box);
			if (result == null) return null;
			level = level(result, box);
		}
		return result;
	}

	private static Language parent(Language language, ImeBox box) {
		return language.parent() != null ? box.languageManager().get(language.parent()) : null;
	}

	public static String type(Release release, Function<String, String> translator) {
		return translator.apply(release.level() == LanguageLevel.L1 ? "Model" : "Language");
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

	public static boolean canRemoveLanguage(Language language, ImeBox box) {
		return box.languageManager().releases(language.name()).isEmpty();
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

	public static String createLanguageLabel(Language language, Function<String, String> translator, ImeBox box) {
		LanguageLevel level = level(language, box);
		if (level == LanguageLevel.L3) return translator.apply("Create language (λ2 or λ1)");
		if (level == LanguageLevel.L2) return translator.apply("Create language (λ1)");
		return null;
	}
}
