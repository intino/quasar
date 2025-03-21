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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class LanguageManager {
	private final Archetype archetype;

	public LanguageManager(Archetype archetype) {
		this.archetype = archetype;
	}

	public List<Language> publicLanguages() {
		return languages().stream().filter(l -> l.owner() == null || l.owner().isEmpty()).toList();
	}

	public List<Language> ownerLanguages(String owner) {
		return languages().stream().filter(l -> l.owner() != null && l.owner().equals(owner)).toList();
	}

	public List<Language> languages() {
		File[] root = archetype.languages().root().listFiles();
		if (root == null) return Collections.emptyList();
		return Arrays.stream(root).filter(File::isDirectory).map(this::languageOf).filter(Objects::nonNull).collect(toList());
	}

	public List<Language> proprietaryLanguages(String user) {
		if (user == null) return emptyList();
		return languages().stream().filter(l -> user.equals(l.owner())).collect(toList());
	}

	public Language create(String name, Language.Level level, String description, File dsl, File logo, String parent, String owner) {
		Language language = new Language(name);
		language.level(level);
		language.description(description);
		language.parent(parent);
		language.owner(owner);
		language.createDate(Instant.now());
		saveDsl(language, dsl);
		saveLogo(language, logo);
		return save(language);
	}

	public void saveLogo(Language language, File logo) {
		try {
			if (logo == null) return;
			Files.move(logo.toPath(), archetype.languages().logo(Language.nameOf(language.name())).toPath());
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public void saveDsl(Language language, File dsl) {
		try {
			if (dsl == null) return;
			Files.move(dsl.toPath(), archetype.languages().dslFile(Language.nameOf(language.name())).toPath());
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public boolean exists(String language) {
		return archetype.languages().properties(Language.nameOf(language)).exists();
	}

	public Language getDefault() {
		return languages().stream().findFirst().orElse(null);
	}

	public Language get(String language) {
		return languageOf(language);
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

	private Language languageOf(File file) {
		return languageOf(file.getName());
	}

	private Language languageOf(String id) {
		try {
			File properties = archetype.languages().properties(Language.nameOf(id));
			if (!properties.exists()) return null;
			Language language = Json.fromJson(Files.readString(properties.toPath()), Language.class);
			language.modelsCountProvider(this::modelsCount);
			return language;
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	private long modelsCount(Language language) {
		File[] models = archetype.languages().models(language.name()).listFiles();
		return models != null ? Arrays.stream(models).filter(d -> !d.getName().startsWith(".")).count() : 0;
	}

}
