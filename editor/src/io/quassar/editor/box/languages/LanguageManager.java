package io.quassar.editor.box.languages;

import io.intino.alexandria.logger.Logger;
import io.quassar.archetype.Archetype;
import io.quassar.editor.box.util.ArtifactoryHelper;
import io.quassar.editor.box.util.Formatters;
import io.quassar.editor.box.util.ImageResizer;
import io.quassar.editor.box.util.SubjectHelper;
import io.quassar.editor.model.*;
import org.apache.commons.io.FileUtils;
import systems.intino.datamarts.subjectstore.SubjectStore;
import systems.intino.datamarts.subjectstore.model.Subject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

public class LanguageManager {
	private final Archetype archetype;
	private final SubjectStore subjectStore;
	private final Function<String, Model> modelProvider;

	public LanguageManager(Archetype archetype, SubjectStore store, Function<String, Model> modelProvider) {
		this.archetype = archetype;
		this.subjectStore = store;
		this.modelProvider = modelProvider;
	}

	public List<Language> visibleLanguages(String owner) {
		Set<Language> result = new HashSet<>(foundationalLanguages());
		result.addAll(privateLanguages(owner));
		result.addAll(licensedLanguages(owner));
		return result.stream().filter(l -> !l.name().equals(Language.Metta)).distinct().toList();
	}

	public List<Language> foundationalLanguages() {
		return languages().stream().filter(Language::isFoundational).toList();
	}

	public List<Language> privateLanguages(String owner) {
		return languages().stream().filter(l -> hasAccess(l, owner)).toList();
	}

	public List<Language> licensedLanguages(String owner) {
		if (owner == null) return Collections.emptyList();
		Set<String> collections = new HashSet<>(subjectStore.query().isType(SubjectHelper.LicenseType).where("user").equals(owner).collect().stream().map(s -> s.parent().name()).distinct().toList());
		collections.addAll(subjectStore.query().isType(SubjectHelper.CollectionType).where("owner").equals(owner).collect().stream().map(Subject::name).toList());
		collections.addAll(subjectStore.query().isType(SubjectHelper.CollectionType).where("collaborator").equals(owner).collect().stream().map(Subject::name).toList());
		if (collections.isEmpty()) return Collections.emptyList();
		return subjectStore.query().isType(SubjectHelper.LanguageType).where("collection").satisfy(v -> collections.stream().anyMatch(v::equals)).stream().map(this::get).toList();
	}

	public List<Language> languages() {
		return subjectStore.query().isType(SubjectHelper.LanguageType).isRoot().collect().stream().map(this::get).toList();
	}

	public List<Language> languages(Collection collection) {
		return subjectStore.query().isType(SubjectHelper.LanguageType).isRoot().where("collection").equals(collection.name()).collect().stream().map(this::get).toList();
	}

	public Language create(Collection collection, String name, Model metamodel, Language.Level level, String title, String description) {
		Language language = new Language(subjectStore.create(SubjectHelper.languagePath(Language.key(collection.id(), name))));
		language.collection(collection.id());
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

	public void renameRelease(Language language, LanguageRelease release, String newVersion) {
		LanguageRelease newRelease = new LanguageRelease(subjectStore.open(SubjectHelper.pathOf(language, release.version())).rename(newVersion));
		newRelease.version(newVersion);
	}

	public void removeRelease(Language language, LanguageRelease release) {
		try {
			Subject subject = subjectStore.create(SubjectHelper.pathOf(language, release.version()));
			if (subject != null) subject.drop();
			File directory = archetype.languages().release(language.key(), release.version());
			if (!directory.exists()) return;
			FileUtils.deleteDirectory(directory);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public LanguageExecution createExecution(Language language, LanguageRelease release, String name, LanguageExecution.Type type) {
		LanguageExecution result = new LanguageExecution(subjectStore.create(SubjectHelper.executionPathOf(language, release)));
		result.name(name);
		result.type(type);
		return result;
	}

	public File loadLogo(Language language) {
		return loadLogo(language, LogoSize.Original);
	}

	public File loadLogo(Language language, LogoSize size) {
		if (size == LogoSize.S50) return createIfNotExists(archetype.languages().logo50(language.key()), language);
		else if (size == LogoSize.S100) return createIfNotExists(archetype.languages().logo100(language.key()), language);
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
				createWebLogos(language);
			}
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public void saveDsl(Language language, String release, File dsl) {
		copy(dsl, archetype.languages().releaseDslJar(language.key(), release));
		LanguageRelease languageRelease = language.release(release);
		ArtifactoryHelper.cleanDslCache(language, languageRelease, archetype.languages());
	}

	public void saveDslManifest(Language language, String release, File dsl) {
		copy(dsl, archetype.languages().releaseDslManifest(language.key(), release));
		LanguageRelease languageRelease = language.release(release);
		ArtifactoryHelper.cleanDslCache(language, languageRelease, archetype.languages());
	}

	public File loadDsl(GavCoordinates coordinates) {
		return loadDsl(coordinates.languageId(), coordinates.version());
	}

	public File loadDsl(Language language, LanguageRelease release) {
		return loadDsl(language.key(), release.version());
	}

	public File loadDslDigest(Language language, LanguageRelease release) {
		File digest = archetype.languages().releaseDslJarDigest(language.key(), release.version());
		if (!digest.exists()) ArtifactoryHelper.prepareDsl(language, release, archetype.languages());
		return digest.exists() ? digest : null;
	}

	public File loadDslManifest(Language language, LanguageRelease release) {
		File manifest = archetype.languages().releaseDslManifest(language.key(), release.version());
		if (!manifest.exists()) ArtifactoryHelper.prepareDsl(language, release, archetype.languages());
		return manifest.exists() ? manifest : null;
	}

	public File loadDslManifestDigest(Language language, LanguageRelease release) {
		File manifest = archetype.languages().releaseDslManifestDigest(language.key(), release.version());
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

	public File loadParser(Language language, LanguageRelease release, String name) {
		if (release == null) return null;
		File result = archetype.languages().releaseParserJar(language.key(), release.version(), name);
		if (!result.exists()) ArtifactoryHelper.prepareParserDependency(language, release, name, archetype.languages());
		return result.exists() ? result : null;
	}

	public File loadParserDigest(Language language, LanguageRelease release, String name) {
		File manifest = archetype.languages().releaseParserJarDigest(language.key(), release.version(), name);
		if (!manifest.exists()) ArtifactoryHelper.prepareParserDependency(language, release, name, archetype.languages());
		return manifest.exists() ? manifest : null;
	}

	public File loadParserManifest(Language language, LanguageRelease release, String name) {
		File manifest = archetype.languages().releaseParserManifest(language.key(), release.version(), name);
		if (!manifest.exists()) ArtifactoryHelper.prepareParserDependency(language, release, name, archetype.languages());
		return manifest.exists() ? manifest : null;
	}

	public File loadParserManifestDigest(Language language, LanguageRelease release, String name) {
		File manifest = archetype.languages().releaseParserManifestDigest(language.key(), release.version(), name);
		if (!manifest.exists()) ArtifactoryHelper.prepareParserDependency(language, release, name, archetype.languages());
		return manifest.exists() ? manifest : null;
	}

	public List<File> loadParsers(Language language, LanguageRelease release) {
		if (release == null) return null;
		return archetype.languages().releaseParsers(language.key(), release.version());
	}

	public void saveParsers(Language language, String release, List<File> parsers) {
		LanguageRelease languageRelease = language.release(release);
		copy(parsers, archetype.languages().releaseParsersDir(language.key(), release));
		parsers.forEach(p -> ArtifactoryHelper.cleanParserDependencyCache(language, languageRelease, p.getName(), archetype.languages()));
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

	public boolean exists(String collection, String language) {
		if (language == null) return false;
		if (new File(archetype.languages().root(), language).exists()) return true;
		if (new File(archetype.languages().root(), Language.key(collection, language)).exists()) return true;
		return !subjectStore.query().isType(SubjectHelper.LanguageType).where("collection").equals(collection).where("name").equals(language).collect().isEmpty();
	}

	public Language getDefault() {
		return languages().stream().findFirst().orElse(null);
	}

	public Language getWithMetamodel(Model model) {
		List<systems.intino.datamarts.subjectstore.model.Subject> result = subjectStore.query().isType(SubjectHelper.LanguageType).where("metamodel").equals(SubjectHelper.modelPath(model.id())).collect();
		if (result.isEmpty()) result = subjectStore.query().isType(SubjectHelper.LanguageType).where("metamodel").equals(model.id()).collect();
		return !result.isEmpty() ? get(result.getFirst()) : null;
	}

	public Language get(Model model) {
		return get(Model.language(subjectStore.open(SubjectHelper.pathOf(model))));
	}

	public Language get(GavCoordinates gav) {
		Language language = get(Language.key(gav.groupId(), gav.artifactId()));
		if (language == null) language = get(gav.artifactId());
		return language;
	}

	public Language get(String id) {
		Language language = get(subjectStore.open(SubjectHelper.languagePath(id)));
		if (language == null) language = get(subjectStore.query().isType(SubjectHelper.LanguageType).where("collection").equals(Language.collectionFrom(id)).where("name").satisfy(n -> Formatters.normalizeLanguageName(n).equalsIgnoreCase(Language.nameFrom(id)) || n.equalsIgnoreCase(Language.nameFrom(id))).collect().stream().findFirst().orElse(null));
		return language;
	}

	public void rename(Language language, String newId) {
		language.collection(Language.collectionFrom(newId));
		language.name(Language.nameFrom(newId));
		subjectStore.open(SubjectHelper.pathOf(language)).rename(newId);
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
		if (language.isPublic()) return true;
		if (user == null) return false;
		Model metamodel = language.metamodel() != null ? modelProvider.apply(language.metamodel()) : null;
		String owner = metamodel != null ? metamodel.owner() : null;
		if ((owner == null || owner.equals(User.Quassar)) && language.isFoundational()) return true;
		if (owner != null && owner.equals(user)) return true;
		if (metamodel == null) return false;
		return metamodel.collaborators().stream().anyMatch(c -> c.equals(user));
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
			for (File file : source) Files.copy(file.toPath(), new File(destiny, file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
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

	private void createWebLogos(Language language) {
		try {
			File logo = archetype.languages().logo(language.key());
			if (!logo.exists()) return;
			resize(logo, 50, archetype.languages().logo50(language.key()));
			resize(logo, 100, archetype.languages().logo100(language.key()));
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private void resize(File logo, int size, File destiny) throws IOException {
		if (destiny.exists()) destiny.delete();
		ImageResizer.resizeImage(logo, size, size, destiny);
	}

	private File createIfNotExists(File logo, Language language) {
		if (!logo.exists()) createWebLogos(language);
		return logo;
	}

}
