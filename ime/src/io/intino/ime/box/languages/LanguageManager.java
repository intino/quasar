package io.intino.ime.box.languages;

import io.intino.alexandria.Json;
import io.intino.alexandria.Resource;
import io.intino.alexandria.logger.Logger;
import io.intino.ime.box.util.VersionNumberComparator;
import io.intino.ime.model.Language;
import io.intino.ime.model.LanguageLevel;
import io.intino.ime.model.Model;
import io.intino.ime.model.Release;
import io.intino.languagearchetype.Archetype;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
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

	public List<Language> allLanguages() {
		File[] root = archetype.languages().root().listFiles();
		if (root == null) return Collections.emptyList();
		return Arrays.stream(root).filter(File::isDirectory).map(this::languageOf).filter(Objects::nonNull).collect(toList());
	}

	public List<Language> publicLanguages() {
		return allLanguages().stream().filter(Language::isPublic).collect(toList());
	}

	public List<Language> publicLanguages(String user) {
		List<Language> result = publicLanguages();
		result.addAll(proprietaryLanguages(user));
		return result;
	}

	public List<Language> proprietaryLanguages(String user) {
		if (user == null) return emptyList();
		return allLanguages().stream().filter(l -> l.isPrivate() && user.equals(l.owner())).collect(toList());
	}

	public Language create(String name, String description, Resource logo, Release parent, String owner, boolean isPrivate) {
		Language language = new Language(name);
		language.description(description);
		language.isPrivate(isPrivate);
		language.parent(parent.id());
		language.owner(owner);
		saveLogo(language, logo);
		return save(language);
	}

	public void saveLogo(Language language, Resource logo) {
		try {
			if (logo == null) return;
			Files.write(archetype.repository().logos().getLogo(Language.nameOf(language.name())).toPath(), logo.bytes());
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public boolean exists(String language) {
		return archetype.languages().definition(Language.nameOf(language)).exists();
	}

	public Language get(String language) {
		return languageOf(language);
	}

	public List<Release> releases(String language) {
		try {
			File releasesFile = archetype.languages().releases(Language.nameOf(language));
			if (!releasesFile.exists()) return emptyList();
			return Files.readAllLines(releasesFile.toPath()).stream().map(this::releaseOf).toList();
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	public List<Release> releases(Model model) {
		return releases(model.releasedLanguage()).stream().filter(r -> r.model().equals(model.id())).toList();
	}

	public Release release(Model model) {
		return releases(model.modelingLanguage()).stream().filter(r -> r.version().equals(Language.versionOf(model.modelingLanguage()))).findFirst().orElse(null);
	}

	public Release lastRelease(Model model) {
		return lastRelease(model.releasedLanguage());
	}

	public Release lastRelease(Language language) {
		return lastRelease(language.name());
	}

	public Release lastRelease(String language) {
		return releases(language).stream().min((o1, o2) -> VersionNumberComparator.getInstance().compare(o2.version(), o1.version())).orElse(null);
	}

	public Release createRelease(Model model, LanguageLevel level, String version) {
		return save(new Release(model.releasedLanguage(), model.id(), level, version));
	}

	public void remove(Language language) {
		try {
			File rootDir = archetype.languages().definition(Language.nameOf(language.name())).getParentFile();
			if (!rootDir.exists()) return;
			FileUtils.deleteDirectory(rootDir);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private Language languageOf(File file) {
		return languageOf(file.getName());
	}

	private Language languageOf(String id) {
		try {
			File definition = archetype.languages().definition(Language.nameOf(id));
			if (!definition.exists()) return null;
			return Json.fromJson(Files.readString(definition.toPath()), Language.class);
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	private Release releaseOf(String content) {
		return Json.fromJson(content, Release.class);
	}

	public Language save(Language language) {
		try {
			Files.writeString(archetype.languages().definition(Language.nameOf(language.name())).toPath(), Json.toString(language));
		} catch (IOException e) {
			Logger.error(e);
		}
		return language;
	}

	public Release save(Release release) {
		try {
			File releasesFile = archetype.languages().releases(Language.nameOf(release.language()));
			if (!releasesFile.exists()) releasesFile.createNewFile();
			Files.writeString(releasesFile.toPath(), Json.toString(release) + "\n", StandardOpenOption.APPEND);
		} catch (IOException e) {
			Logger.error(e);
		}
		return release;
	}

}
