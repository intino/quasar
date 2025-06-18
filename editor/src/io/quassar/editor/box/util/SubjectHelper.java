package io.quassar.editor.box.util;

import io.quassar.editor.model.*;
import systems.intino.datamarts.subjectstore.model.Subject;
import systems.intino.datamarts.subjectstore.model.Term;

import java.util.List;
import java.util.stream.Collectors;

public class SubjectHelper {
	public static final String CollectionType = "collection";
	public static final String LanguageType = "dsl";
	public static final String LanguageReleaseType = "release";
	public static final String LanguageExecutionType = "execution";
	public static final String ModelType = "model";
	public static final String UserType = "user";
	public static final String ModelReleaseType = "release";
	public static final String LicenseType = "license";

	public static final String CollectionPath = "%s.collection";
	public static final String CollectionLicensePath = "%s.collection/%s.license";
	public static final String LanguagePath = "%s.dsl";
	public static final String LanguageReleasePath = "%s.dsl/%s.release";
	public static final String LanguageExecutionPath = "%s.dsl/%s.release/0001.execution";
	public static final String ModelPath = "%s.model";
	public static final String ModelReleasePath = "%s.model/%s.release";
	public static final String UserPath = "%s.user";

	public static String collectionPath(String name) {
		return CollectionPath.formatted(name);
	}

	public static String pathOf(Collection collection) {
		return CollectionPath.formatted(collection.name());
	}

	public static String pathOf(Collection collection, String license) {
		return CollectionLicensePath.formatted(collection.name(), license);
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

	public static String executionPathOf(Language language, LanguageRelease release) {
		return LanguageExecutionPath.formatted(language.id(), release.version());
	}

	public static String pathOf(Language language, String version) {
		return LanguageReleasePath.formatted(language.id(), version);
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
