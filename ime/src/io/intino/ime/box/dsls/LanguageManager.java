package io.intino.ime.box.dsls;

import io.intino.ime.model.Language;
import io.intino.ime.model.LanguageLevel;
import io.intino.languagearchetype.Archetype;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class LanguageManager {
	private final Archetype archetype;
	private final Map<String, Language> languageMap;

	public LanguageManager(Archetype archetype) {
		this.archetype = archetype;
		this.languageMap = load();
	}

	public List<Language> publicLanguages() {
		return languageMap.values().stream().filter(Language::isPublic).collect(toList());
	}

	public List<Language> publicLanguages(String user) {
		List<Language> result = publicLanguages();
		result.addAll(proprietaryLanguages(user));
		return result;
	}

	public List<Language> proprietaryLanguages(String user) {
		if (user == null) return emptyList();
		return languageMap.values().stream().filter(l -> user.equals(l.owner())).collect(toList());
	}

	public Language load(String id) {
		return new Language(id);
	}

	private Map<String, Language> load() {
		try {
			File languages = archetype.languages();
			return Files.readAllLines(languages.toPath()).stream().skip(1).collect(toMap(l -> l.split("\t")[0], this::languageFrom));
		} catch (IOException e) {
			return emptyMap();
		}
	}

	private Language languageFrom(String line) {
		String[] split = line.split("\t");
		LanguageLevel level = LanguageLevel.from(split[1]);
		boolean isPrivate = !Boolean.parseBoolean(split[3]);
		Instant createDate = Instant.ofEpochMilli(Long.parseLong(split[4]));
		return new Language(split[0]).level(level).owner(split[2]).isPrivate(isPrivate).createDate(createDate);
	}

}
