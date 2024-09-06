package io.intino.ime.box.languages;

import io.intino.alexandria.logger.Logger;
import io.intino.ime.model.Language;
import io.intino.ime.model.LanguageInfo;
import io.intino.ime.model.Model;
import io.intino.languagearchetype.Archetype;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.*;

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
		return languageMap.values().stream().filter(l -> l.isPrivate() && user.equals(l.owner())).collect(toList());
	}

	public List<Language> versions(String language) {
		return languageMap.values().stream().filter(l -> l.name().equals(language)).collect(toList());
	}

	public Language create(Model model, String name, String version, String owner, Instant createDate) {
		Language metaLanguage = get(model.language());
		String id = Language.id(name, version);
		LanguageInfo.Level level = metaLanguage.info().level() == LanguageInfo.Level.L3 ? LanguageInfo.Level.L2 : LanguageInfo.Level.L1;
		LanguageInfo info = new LanguageInfo().level(level).metaLanguage(metaLanguage.id());
		return save(new Language(id).info(info).isPrivate(true).createDate(createDate).owner(owner));
	}

	public Language save(Language language) {
		languageMap.put(language.id(), language);
		save();
		return language;
	}

	public Language get(String id) {
		return languageMap.containsKey(id) ? languageMap.get(id) : defaultLanguage();
	}

	public void remove(Language language) {
		if (!languageMap.containsKey(language.id())) return;
		languageMap.remove(language.id());
		save();
	}

	private Language defaultLanguage() {
		return new Language("none:1.0.0").info(LanguageInfo.from("L1(none:1.0.0-SNAPSHOT)"));
	}

	private Map<String, Language> load() {
		try {
			File languages = archetype.languages();
			return Files.readAllLines(languages.toPath()).stream().skip(1).collect(toMap(l -> l.split("\t")[0], this::languageFrom));
		} catch (IOException e) {
			Logger.error(e);
			return emptyMap();
		}
	}

	private void save() {
		try {
			File languages = archetype.languages();
			String header = "Id\tLevel\tOwner\tPublic\tCreateDate";
			String content = languageMap.values().stream().map(this::serialize).collect(joining("\n"));
			Files.writeString(languages.toPath(), header + "\n" + content);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private String serialize(Language language) {
		return language.id() + "\t" + language.info().toString() + "\t" + language.owner() + "\t" +
				language.isPublic() + "\t" + language.createDate().toEpochMilli();
	}

	private Language languageFrom(String line) {
		String[] split = line.split("\t");
		LanguageInfo level = LanguageInfo.from(split[1]);
		boolean isPrivate = !Boolean.parseBoolean(split[3]);
		Instant createDate = Instant.ofEpochMilli(Long.parseLong(split[4]));
		return new Language(split[0]).info(level).owner(split[2]).isPrivate(isPrivate).createDate(createDate);
	}

	public boolean exists(String name) {
		return languageMap.keySet().stream().anyMatch(l -> l.startsWith(name + ":"));
	}

}
