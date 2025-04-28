package io.quassar.editor.box.languages;

import io.intino.alexandria.logger.Logger;
import io.quassar.archetype.Archetype;
import io.quassar.editor.box.util.SubjectHelper;
import io.quassar.editor.model.*;
import org.apache.commons.io.FileUtils;
import systems.intino.datamarts.subjectstore.SubjectStore;
import systems.intino.datamarts.subjectstore.model.Subject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class LanguageManager {
	private final Archetype archetype;
	private final SubjectStore subjectStore;

	public LanguageManager(Archetype archetype, SubjectStore store) {
		this.archetype = archetype;
		this.subjectStore = store;
	}

	public List<Language> visibleLanguages(String owner) {
		Set<Language> result = new HashSet<>(quassarLanguages());
		result.addAll(privateLanguages(owner));
		return result.stream().filter(l -> !l.name().equals(Language.Metta)).distinct().toList();
	}

	public List<Language> quassarLanguages() {
		return languages().stream().filter(Language::isQuassarLanguage).toList();
	}

	public List<Language> privateLanguages(String owner) {
		return languages().stream().filter(l -> hasAccess(l, owner)).toList();
	}

	public List<Language> languages() {
		return subjectStore.subjects().type(SubjectHelper.LanguageType).isRoot().collect().stream().map(this::get).toList();
	}

	public Language create(String group, String name, Model metamodel, Language.Level level, String title, String description) {
		Language language = new Language(subjectStore.create(SubjectHelper.languagePath(group, name)));
		language.group(group);
		language.name(name);
		language.level(level);
		language.title(title);
		language.description(description);
		language.createDate(Instant.now());
		if (metamodel == null) return language;
		language.metamodel(metamodel.id());
		language.parent(metamodel.language());
		return language;
	}

	public LanguageRelease createRelease(Language language, String version, File dsl) {
		LanguageRelease release = new LanguageRelease(subjectStore.create(SubjectHelper.pathOf(language, version)));
		release.version(version);
		saveDsl(language, version, dsl);
		return release;
	}

	public LanguageTool createReleaseTool(Language language, LanguageRelease release, String name, LanguageTool.Type type, Map<String, String> parameters) {
		LanguageTool tool = new LanguageTool(subjectStore.create(SubjectHelper.pathOf(language, release, name)));
		tool.type(type);
		parameters.forEach((key, value) -> createReleaseToolParameter(language, release, tool, key, value));
		return tool;
	}

	private void createReleaseToolParameter(Language language, LanguageRelease release, LanguageTool tool, String key, String value) {
		LanguageTool.Parameter parameter = new LanguageTool.Parameter(subjectStore.create(SubjectHelper.pathOf(language, release, tool, key)));
		parameter.value(value);
	}

	public boolean removeReleaseTool(Language language, LanguageRelease release, LanguageTool tool) {
		Subject subject = subjectStore.create(SubjectHelper.pathOf(language, release, tool));
		subject.drop();
		return true;
	}

	public File loadLogo(Language language) {
		return archetype.languages().logo(language.id());
	}

	public void saveLogo(Language language, File logo) {
		try {
			File current = archetype.languages().logo(language.id());
			if (logo == null && current.exists()) { current.delete(); return; }
			if (logo != null && logo.getAbsolutePath().equals(current.getAbsolutePath())) return;
			if (logo != null) {
				if (current.exists()) current.delete();
				Files.move(logo.toPath(), current.toPath());
			}
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public void saveDsl(Language language, String release, File dsl) {
		try {
			if (dsl == null) return;
			File destiny = archetype.languages().releaseDsl(language.id(), release);
			if (destiny.exists()) destiny.delete();
			Files.copy(dsl.toPath(), destiny.toPath());
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public String loadHelp(Language language, String version) {
		return loadHelp(language.id(), version);
	}

	public String loadHelp(Language language, LanguageRelease release) {
		return loadHelp(language.id(), release.version());
	}

	public String loadHelp(GavCoordinates gav) {
		return loadHelp(gav.languageId(), gav.version());
	}

	public void saveHelp(Language language, String release, String content) {
		try {
			if (content == null) return;
			File destiny = archetype.languages().releaseHelp(language.id(), release);
			Files.writeString(destiny.toPath(), content);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public File loadReader(Language language, LanguageRelease release, String programmingLanguage) {
		if (release == null) return null;
		List<File> readers = archetype.languages().releaseReaders(language.id(), release.version());
		return readers.stream().filter(r -> r.getName().startsWith(programmingLanguage + ".")).findFirst().orElse(null);
	}

	public List<File> loadReaders(Language language, LanguageRelease release) {
		if (release == null) return null;
		return archetype.languages().releaseReaders(language.id(), release.version());
	}

	public boolean exists(Model model) {
		return get(model) != null;
	}

	public boolean exists(String language) {
		if (new File(archetype.languages().root(), language).exists()) return true;
		return !subjectStore.subjects().type(SubjectHelper.LanguageType).with("name", language).collect().isEmpty();
	}

	public Language getDefault() {
		return languages().stream().findFirst().orElse(null);
	}

	public Language getWithMetamodel(Model model) {
		List<systems.intino.datamarts.subjectstore.model.Subject> result = subjectStore.subjects().type(SubjectHelper.LanguageType).with("metamodel", SubjectHelper.modelPath(model.id())).collect();
		if (result.isEmpty()) result = subjectStore.subjects().type(SubjectHelper.LanguageType).with("metamodel", model.id()).collect();
		return !result.isEmpty() ? get(result.getFirst()) : null;
	}

	public Language get(Model model) {
		return get(Model.language(subjectStore.open(SubjectHelper.pathOf(model))));
	}

	public Language get(GavCoordinates gav) {
		return get(gav.languageId());
	}

	public Language get(String id) {
		return get(subjectStore.open(SubjectHelper.languagePath(id)));
	}

	public void remove(Language language) {
		try {
			File rootDir = archetype.languages().get(language.id());
			if (!rootDir.exists()) return;
			language.releases().forEach(r -> subjectStore.open(SubjectHelper.pathOf(language, r)).drop());
			subjectStore.open(SubjectHelper.pathOf(language)).drop();
			FileUtils.deleteDirectory(rootDir);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public boolean hasAccess(Language language, String user) {
		if (user == null) return false;
		String owner = owner(language);
		if (owner == null && language.isQuassarLanguage()) return true;
		if (owner != null && owner.equals(user)) return true;
		List<String> patternList = language.grantAccessList();
		return patternList.stream().anyMatch(p -> matches(p, user));
	}

	public String owner(Language language) {
		String metamodel = language.metamodel();
		return metamodel != null ? Model.owner(subjectStore.open(SubjectHelper.modelPath(metamodel))) : null;
	}

	private boolean matches(String regex, String user) {
		Pattern pattern = Pattern.compile(regex);
		return pattern.matcher(user).matches();
	}

	private String loadHelp(String language, String release) {
		try {
			if (release == null) return null;
			File helpFile = archetype.languages().releaseHelp(language, release);
			if (!helpFile.exists()) return null;
			return Files.readString(helpFile.toPath());
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	private Language get(Subject subject) {
		if (subject == null || subject.isNull()) return null;
		return new Language(subject);
	}

}
