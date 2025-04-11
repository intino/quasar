package io.quassar.editor.box.util;

import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import io.quassar.editor.model.Model;
import io.quassar.editor.model.User;

public class SubjectHelper {
	public static final String LanguageType = "language";
	public static final String LanguageReleaseType = "lr";
	public static final String ModelType = "model";
	public static final String ModelReleaseType = "mr";
	public static final String UserType = "user";

	public static final String LanguagePath = "%s.language";
	public static final String LanguageReleasePath = "%s.language/%s.lr";
	public static final String ModelPath = "%s.model";
	public static final String ModelReleasePath = "%s.language/%s.mr";
	public static final String UserPath = "%s.user";

	public static String languagePath(String group, String name) {
		return LanguagePath.formatted(Language.id(group, name));
	}

	public static String languagePath(String id) {
		return LanguagePath.formatted(id);
	}

	public static String pathOf(Language language) {
		return LanguagePath.formatted(language.id());
	}

	public static String pathOf(Language language, LanguageRelease release) {
		return LanguageReleasePath.formatted(language.id(), release.version());
	}

	public static String pathOf(Language language, String version) {
		return LanguageReleasePath.formatted(language.id(), version);
	}

	public static String modelPath(String id) {
		return ModelPath.formatted(id);
	}

	public static String pathOf(Model model) {
		return ModelPath.formatted(model.id());
	}

	public static String pathOf(Model model, String version) {
		return ModelReleasePath.formatted(model.id(), version);
	}

	public static String userPath(String id) {
		return UserPath.formatted(id);
	}

	public static String pathOf(User user) {
		return UserPath.formatted(user.id());
	}

}
