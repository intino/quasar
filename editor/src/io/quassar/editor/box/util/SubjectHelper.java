package io.quassar.editor.box.util;

import io.quassar.editor.model.*;
import systems.intino.datamarts.subjectstore.model.Subject;
import systems.intino.datamarts.subjectstore.model.Term;

import java.util.List;
import java.util.stream.Collectors;

public class SubjectHelper {
	public static final String LanguageType = "language";
	public static final String LanguageReleaseType = "release";
	public static final String LanguageReleaseTool = "tool";
	public static final String LanguageToolParameter = "parameter";
	public static final String ModelType = "model";
	public static final String ModelReleaseType = "release";
	public static final String UserType = "user";

	public static final String LanguagePath = "%s.language";
	public static final String LanguageReleasePath = "%s.language/%s.release";
	public static final String LanguageReleaseToolPath = "%s.language/%s.release/%s.tool";
	public static final String LanguageToolParameterPath = "%s.language/%s.release/%s.tool/%s.parameter";
	public static final String ModelPath = "%s.model";
	public static final String ModelReleasePath = "%s.model/%s.release";
	public static final String UserPath = "%s.user";

	public static String languagePath(String group, String name) {
		return LanguagePath.formatted(Language.key(group, name));
	}

	public static String languagePath(String id) {
		return LanguagePath.formatted(id);
	}

	public static String pathOf(Language language) {
		return LanguagePath.formatted(language.key());
	}

	public static String pathOf(Language language, LanguageRelease release) {
		return LanguageReleasePath.formatted(language.key(), release.version());
	}

	public static String pathOf(Language language, LanguageRelease release, LanguageTool tool) {
		return pathOf(language, release, tool.name());
	}

	public static String pathOf(Language language, LanguageRelease release, String tool) {
		return LanguageReleaseToolPath.formatted(language.key(), release.version(), tool);
	}

	public static String pathOf(Language language, LanguageRelease release, LanguageTool tool, String parameter) {
		return LanguageToolParameterPath.formatted(language.key(), release.version(), tool.name(), parameter);
	}

	public static String pathOf(Language language, String version) {
		return LanguageReleasePath.formatted(language.key(), version);
	}

	public static String modelPath(String id) {
		if (id != null && id.endsWith(ModelPath.formatted(""))) return id;
		return ModelPath.formatted(id);
	}

	public static String pathOf(Model model) {
		return modelPath(model.id());
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

	public static List<String> terms(Subject subject, String name) {
		return subject.terms().stream().filter(t -> t.tag().equals(name)).map(Term::value).collect(Collectors.toList());
	}

}
