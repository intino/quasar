package io.quassar.editor.box.languages;

import io.intino.alexandria.logger.Logger;
import io.quassar.archetype.Archetype;
import io.quassar.editor.box.util.ArtifactoryHelper;
import io.quassar.editor.box.util.SubjectHelper;
import io.quassar.editor.model.*;
import org.apache.commons.io.FileUtils;
import systems.intino.datamarts.subjectstore.SubjectStore;
import systems.intino.datamarts.subjectstore.model.Subject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
		return languages().stream().filter(Language::isFoundational).toList();
	}

	public List<Language> privateLanguages(String owner) {
		return languages().stream().filter(l -> hasAccess(l, owner)).toList();
	}

	public List<Language> languages() {
		return subjectStore.subjects().type(SubjectHelper.LanguageType).isRoot().collect().stream().map(this::get).toList();
	}

	public Language create(String group, String name, Model metamodel, Language.Level level, String title, String description) {
		Language language = new Language(subjectStore.create(SubjectHelper.languagePath(name)));
		language.group(group);
		language.name(name.toLowerCase());
		language.level(level);
		language.title(title);
		language.description(description);
		language.createDate(Instant.now());
		if (metamodel == null) return language;
		language.metamodel(metamodel.id());
		language.parent(metamodel.language());
		return language;
	}

	public LanguageRelease createRelease(Language language, String version) {
		LanguageExecution lastExecution = language.lastRelease() != null ? language.lastRelease().execution() : null;
		LanguageRelease release = new LanguageRelease(subjectStore.create(SubjectHelper.pathOf(language, version)));
		release.version(version);
		if (lastExecution != null) copyExecution(language, release, lastExecution);
		else createExecution(language, release, null, LanguageExecution.Type.None);
		return release;
	}

	public LanguageExecution createExecution(Language language, LanguageRelease release, String name, LanguageExecution.Type type) {
		LanguageExecution result = new LanguageExecution(subjectStore.create(SubjectHelper.executionPathOf(language, release)));
		result.name(name);
		result.type(type);
		return result;
	}

	public File loadLogo(Language language) {
		return archetype.languages().logo(language.key());
	}

	public void saveLogo(Language language, File logo) {
		try {
			File current = archetype.languages().logo(language.key());
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
		copy(dsl, archetype.languages().releaseDslJar(language.key(), release));
	}

	public File loadDsl(GavCoordinates coordinates) {
		return loadDsl(coordinates.languageId(), coordinates.version());
	}

	public File loadDsl(Language language, LanguageRelease release) {
		return loadDsl(language.key(), release.version());
	}

	public File loadDslManifest(Language language, LanguageRelease release) {
		File manifest = archetype.languages().releaseDslManifest(language.key(), release.version());
		if (!manifest.exists()) ArtifactoryHelper.prepareDsl(language, release, archetype.languages());
		return manifest.exists() ? manifest : null;
	}

	public File loadGraph(Language language, LanguageRelease release) {
		if (release == null) return null;
		File file = archetype.languages().releaseGraph(language.key(), release.version());
		if (!file.exists()) return null;
		return file;
	}

	public void saveGraph(Language language, String release, File graph) {
		copy(graph, archetype.languages().releaseGraph(language.key(), release));
	}

	public File loadReader(Language language, LanguageRelease release, String name) {
		if (release == null) return null;
		File result = archetype.languages().releaseReaderJar(language.key(), release.version(), name);
		if (!result.exists()) ArtifactoryHelper.prepareReaderDependency(language, release, name, archetype.languages());
		return result.exists() ? result : null;
	}

	public File loadReaderManifest(Language language, LanguageRelease release, String name) {
		File manifest = archetype.languages().releaseReaderManifest(language.key(), release.version(), name);
		if (!manifest.exists()) ArtifactoryHelper.prepareReaderDependency(language, release, name, archetype.languages());
		return manifest.exists() ? manifest : null;
	}

	public List<File> loadReaders(Language language, LanguageRelease release) {
		if (release == null) return null;
		return archetype.languages().releaseReaders(language.key(), release.version());
	}

	public void saveReaders(Language language, String release, List<File> readers) {
		copy(readers, archetype.languages().releaseReadersDir(language.key(), release));
	}

	public String loadHelp(Language language, String version) {
		return loadHelp(language.key(), version);
	}

	public String loadHelp(Language language, LanguageRelease release) {
		return loadHelp(language.key(), release.version());
	}

	public String loadHelp(GavCoordinates gav) {
		return loadHelp(gav.languageId(), gav.version());
	}

	public void saveHelp(Language language, String release, String content) {
		try {
			if (content == null) return;
			File destiny = archetype.languages().releaseHelp(language.key(), release);
			Files.writeString(destiny.toPath(), content);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public boolean exists(Model model) {
		return get(model) != null;
	}

	public boolean exists(String language) {
		if (new File(archetype.languages().root(), language).exists()) return true;
		return !subjectStore.subjects().type(SubjectHelper.LanguageType).where("name").equals(language).collect().isEmpty();
	}

	public Language getDefault() {
		return languages().stream().findFirst().orElse(null);
	}

	public Language getWithMetamodel(Model model) {
		List<systems.intino.datamarts.subjectstore.model.Subject> result = subjectStore.subjects().type(SubjectHelper.LanguageType).where("metamodel").equals(SubjectHelper.modelPath(model.id())).collect();
		if (result.isEmpty()) result = subjectStore.subjects().type(SubjectHelper.LanguageType).where("metamodel").equals(model.id()).collect();
		return !result.isEmpty() ? get(result.getFirst()) : null;
	}

	public Language get(Model model) {
		return get(Model.language(subjectStore.open(SubjectHelper.pathOf(model))));
	}

	public Language get(GavCoordinates gav) {
		return get(gav.artifactId());
	}

	public Language get(String key) {
		Language language = get(subjectStore.open(SubjectHelper.languagePath(key)));
		if (language == null) language = get(subjectStore.subjects().type(SubjectHelper.LanguageType).where("group").equals(Language.groupFrom(key)).where("name").equals(Language.nameFrom(key)).collect().stream().findFirst().orElse(null));
		return language;
	}

	public void remove(Language language) {
		try {
			File rootDir = archetype.languages().get(language.key());
			if (!rootDir.exists()) return;
			language.releases().forEach(r -> subjectStore.open(SubjectHelper.pathOf(language, r)).drop());
			subjectStore.open(SubjectHelper.pathOf(language)).drop();
			FileUtils.deleteDirectory(rootDir);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public boolean hasAccess(Language language, String user) {
		if (language.isPublic() && language.grantAccessList().isEmpty()) return true;
		if (user == null) return false;
		String owner = owner(language);
		if ((owner == null || owner.equals(User.Quassar)) && language.isFoundational()) return true;
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

	private void copy(File source, File destiny) {
		try {
			if (source == null) return;
			if (destiny.exists()) destiny.delete();
			Files.copy(source.toPath(), destiny.toPath());
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private void copy(List<File> source, File destiny) {
		try {
			if (!destiny.exists()) destiny.mkdirs();
			for (File file : source) Files.copy(file.toPath(), new File(destiny, file.getName()).toPath());
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private File loadDsl(String language, String release) {
		if (release == null) return null;
		File file = archetype.languages().releaseDslJar(language, release);
		if (!file.exists()) return null;
		return file;
	}

	private void copyExecution(Language language, LanguageRelease release, LanguageExecution execution) {
		LanguageExecution result = createExecution(language, release, execution.name(), execution.type());
		result.content(execution.content(LanguageExecution.Type.Local));
		result.content(execution.content(LanguageExecution.Type.Remote));
	}

}
