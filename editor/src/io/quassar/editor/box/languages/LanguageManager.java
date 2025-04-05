package io.quassar.editor.box.languages;

import io.intino.alexandria.Json;
import io.intino.alexandria.logger.Logger;
import io.quassar.archetype.Archetype;
import io.quassar.editor.model.Language;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

public class LanguageManager {
	private final Archetype archetype;
	private final Function<Language, Language.ModelsProvider> modelsProvider;

	public LanguageManager(Archetype archetype, Function<Language, Language.ModelsProvider> modelsProvider) {
		this.archetype = archetype;
		this.modelsProvider = modelsProvider;
	}

	public List<Language> visibleLanguages(String owner) {
		Set<Language> result = new HashSet<>(publicLanguages());
		result.addAll(restrictedLanguages(owner));
		return result.stream().filter(l -> !l.name().equals(Language.Meta)).distinct().toList();
	}

	public List<Language> publicLanguages() {
		return languages().stream().filter(Language::isPublic).toList();
	}

	public List<Language> restrictedLanguages(String owner) {
		return languages().stream().filter(l -> l.isPrivate() && hasAccess(l, owner)).toList();
	}

	public List<Language> languages() {
		File[] root = archetype.languages().root().listFiles();
		if (root == null) return Collections.emptyList();
		return Arrays.stream(root).filter(File::isDirectory).map(this::languageOf).filter(Objects::nonNull).collect(toList());
	}

	public Language create(String name, String version, Language.Level level, String hint, String description, File dsl, String parent, String owner) {
		Language language = new Language(name);
		language.version(version);
		language.level(level);
		language.hint(hint);
		language.description(description);
		language.parent(parent);
		language.owner(owner);
		language.createDate(Instant.now());
		saveDsl(language, dsl);
		return save(language);
	}

	public void saveLogo(Language language, File logo) {
		try {
			File current = archetype.languages().logo(Language.nameOf(language.name()));
			if (logo == null && current.exists()) { current.delete(); return; }
			if (logo != null && logo.getAbsolutePath().equals(current.getAbsolutePath())) return;
			if (logo != null) Files.move(logo.toPath(), current.toPath());
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public void saveDsl(Language language, File dsl) {
		try {
			if (dsl == null) return;
			File destiny = archetype.languages().dslFile(Language.nameOf(language.name()));
			if (destiny.exists()) destiny.delete();
			Files.copy(dsl.toPath(), destiny.toPath());
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public boolean exists(String language) {
		return new File(archetype.languages().root(), Language.nameOf(language)).exists();
	}

	public Language getDefault() {
		return languages().stream().findFirst().orElse(null);
	}

	public Language get(String language) {
		return languageOf(language);
	}

	public void makePrivate(Language language, List<String> accessPatterns) {
		language.isPrivate(true);
		language.accessPatterns(accessPatterns);
		save(language);
	}

	public void makePublic(Language language) {
		language.isPrivate(false);
		save(language);
	}

	public void remove(Language language) {
		try {
			File rootDir = archetype.languages().language(Language.nameOf(language.name()));
			if (!rootDir.exists()) return;
			FileUtils.deleteDirectory(rootDir);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public Language save(Language language) {
		try {
			Files.writeString(archetype.languages().properties(Language.nameOf(language.name())).toPath(), Json.toString(language));
		} catch (IOException e) {
			Logger.error(e);
		}
		return language;
	}

	public boolean hasAccess(Language language, String user) {
		if (user == null) return false;
		if (language.owner() != null && language.owner().equals(user)) return true;
		List<String> patternList = language.accessPatterns();
		return patternList.stream().anyMatch(p -> matches(p, user));
	}

	private Language languageOf(File file) {
		return languageOf(file.getName());
	}

	private Language languageOf(String id) {
		try {
			if (!exists(id)) return null;
			File properties = archetype.languages().properties(Language.nameOf(id));
			if (!properties.exists()) return null;
			Language language = Json.fromJson(Files.readString(properties.toPath()), Language.class);
			language.modelsProvider(modelsProvider.apply(language));
			return language;
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	private boolean matches(String regex, String user) {
		Pattern pattern = Pattern.compile(regex);
		return pattern.matcher(user).matches();
	}

}
