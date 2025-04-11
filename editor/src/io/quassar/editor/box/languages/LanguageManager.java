package io.quassar.editor.box.languages;

import io.intino.alexandria.Json;
import io.intino.alexandria.logger.Logger;
import io.quassar.archetype.Archetype;
import io.quassar.editor.box.util.ArchetypeHelper;
import io.quassar.editor.model.*;
import org.apache.commons.io.FileUtils;
import systems.intino.alexandria.datamarts.anchormap.AnchorMap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class LanguageManager {
	private final Archetype archetype;
	private final AnchorMap index;

	public LanguageManager(Archetype archetype, AnchorMap index) {
		this.archetype = archetype;
		this.index = index;
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
		File[] root = archetype.languages().root().listFiles();
		if (root == null) return emptyList();
		return Arrays.stream(root).filter(File::isDirectory).map(this::languageOf).filter(Objects::nonNull).collect(toList());
	}

	public Language create(String group, String name, Model metamodel, Language.Level level, String title, String description) {
		Language language = new Language(group, name);
		language.metamodel(metamodel.id());
		language.level(level);
		language.title(title);
		language.description(description);
		language.parent(metamodel.language());
		language.createDate(Instant.now());
		return save(language);
	}

	public LanguageRelease createRelease(Language language, String version, File dsl) {
		LanguageRelease release = new LanguageRelease().version(version);
		language.add(release);
		save(language);
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
		List<String> result = index.search("language").with("name", normalize(language)).execute();
		if (!result.isEmpty()) return true;
		return !index.get(normalize(language), "language").isEmpty();
	}

	public Language getDefault() {
		return languages().stream().findFirst().orElse(null);
	}

	public Language get(String language) {
		return languageOf(language);
	}

	public Language getWithMetamodel(Model model) {
		List<String> result = index.search("language").with("metamodel", model.id()).execute();
		return !result.isEmpty() ? get(result.getFirst().replace(":language", "")) : null;
	}

	public Language get(Model model) {
		List<String> predicates = index.get(model.id(), "model");
		return predicates.stream().filter(p -> p.startsWith("language=")).map(p -> get(p.replace("language=", ""))).findFirst().orElse(null);
	}

	public Language get(GavCoordinates gav) {
		return get(gav.languageId());
	}

	public void remove(Language language) {
		try {
			File rootDir = archetype.languages().get(normalize(language.id()));
			if (!rootDir.exists()) return;
			FileUtils.deleteDirectory(rootDir);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public Language save(Language language) {
		try {
			index.on(language.id(), "language").set("metamodel", language.metamodel()).set("name", language.name()).commit();
			Files.writeString(archetype.languages().properties(normalize(language.id())).toPath(), Json.toString(language));
		} catch (IOException e) {
			Logger.error(e);
		}
		return language;
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
		return ownerOf(index.get(language.metamodel(), "model"));
	}

	private String ownerOf(List<String> values) {
		return values.stream().filter(v -> v.startsWith("owner=")).map(v -> v.replace("owner=", "")).findFirst().orElse(null);
	}

	private Language languageOf(File file) {
		return languageOf(file.getName());
	}

	private Language languageOf(String id) {
		try {
			if (!exists(id)) return null;
			File properties = archetype.languages().properties(normalize(id));
			if (!properties.exists()) return null;
			return Json.fromJson(Files.readString(properties.toPath()), Language.class);
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
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

}
