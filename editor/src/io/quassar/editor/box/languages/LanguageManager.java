package io.quassar.editor.box.languages;

import io.intino.alexandria.logger.Logger;
import io.quassar.archetype.Archetype;
import io.quassar.editor.box.util.ArchetypeHelper;
import io.quassar.editor.box.util.SubjectHelper;
import io.quassar.editor.model.GavCoordinates;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import io.quassar.editor.model.Model;
import org.apache.commons.io.FileUtils;
import systems.intino.datamarts.subjectindex.SubjectTree;
import systems.intino.datamarts.subjectindex.model.Subject;
import systems.intino.datamarts.subjectindex.model.Subjects;
import systems.intino.datamarts.subjectindex.model.Tokens;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class LanguageManager {
	private final Archetype archetype;
	private final SubjectTree subjectTree;

	public LanguageManager(Archetype archetype, SubjectTree subjectTree) {
		this.archetype = archetype;
		this.subjectTree = subjectTree;
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
		return subjectTree.subjects(SubjectHelper.LanguageType).roots().stream().map(this::get).toList();
	}

	public Language create(String group, String name, Model metamodel, Language.Level level, String title, String description) {
		Language language = new Language(subjectTree.create(SubjectHelper.languagePath(group, name)));
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
		LanguageRelease release = new LanguageRelease(subjectTree.create(SubjectHelper.pathOf(language, version)));
		release.version(version);
		saveDsl(language, version, dsl);
		return release;
	}

	public File loadLogo(Language language) {
		return archetype.languages().logo(normalize(language.id()));
	}

	public void saveLogo(Language language, File logo) {
		try {
			File current = archetype.languages().logo(normalize(language.id()));
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
			File destiny = archetype.languages().releaseDsl(normalize(language.id()), release);
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
			File destiny = archetype.languages().releaseHelp(normalize(language.id()), release);
			Files.writeString(destiny.toPath(), content);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public File loadReader(Language language, LanguageRelease release, String programmingLanguage) {
		if (release == null) return null;
		List<File> readers = archetype.languages().releaseReaders(normalize(language.id()), release.version());
		return readers.stream().filter(r -> r.getName().startsWith(programmingLanguage + ".")).findFirst().orElse(null);
	}

	public List<File> loadReaders(Language language, LanguageRelease release) {
		if (release == null) return null;
		return archetype.languages().releaseReaders(normalize(language.id()), release.version());
	}

	public boolean exists(Model model) {
		return get(model) != null;
	}

	public boolean exists(String language) {
		if (new File(archetype.languages().root(), normalize(language)).exists()) return true;
		return !subjectTree.subjects(SubjectHelper.LanguageType).with("name", normalize(language)).roots().isEmpty();
	}

	public Language getDefault() {
		return languages().stream().findFirst().orElse(null);
	}

	public Language getWithMetamodel(Model model) {
		Subjects result = subjectTree.subjects(SubjectHelper.LanguageType).with("metamodel", model.id()).roots();
		return !result.isEmpty() ? get(result.get(0)) : null;
	}

	public Language get(Model model) {
		Subject subject = subjectTree.get(SubjectHelper.pathOf(model));
		Tokens.Values values = subject.tokens().get("language");
		return values.iterator().hasNext() ? get(values.first()) : null;
	}

	public Language get(GavCoordinates gav) {
		return get(gav.languageId());
	}

	public Language get(String id) {
		return get(subjectTree.get(SubjectHelper.languagePath(id)));
	}

	public void remove(Language language) {
		try {
			File rootDir = archetype.languages().get(normalize(language.id()));
			if (!rootDir.exists()) return;
			language.releases().forEach(r -> subjectTree.drop(SubjectHelper.pathOf(language, r)));
			subjectTree.drop(SubjectHelper.pathOf(language));
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
		Subject subject = subjectTree.get(SubjectHelper.modelPath(language.metamodel()));
		return subject != null && subject.tokens().get("owner").iterator().hasNext() ? subject.tokens().get("owner").first() : null;
	}

	private boolean matches(String regex, String user) {
		Pattern pattern = Pattern.compile(regex);
		return pattern.matcher(user).matches();
	}

	private String loadHelp(String language, String release) {
		try {
			if (release == null) return null;
			File helpFile = archetype.languages().releaseHelp(normalize(language), release);
			if (!helpFile.exists()) return null;
			return Files.readString(helpFile.toPath());
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	private String normalize(String language) {
		return ArchetypeHelper.languageDirectoryName(language);
	}

	private Language get(Subject subject) {
		if (subject == null || subject.isNull()) return null;
		return new Language(subject);
	}

}
