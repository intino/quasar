package io.quassar.editor.box.languages;

import io.intino.alexandria.logger.Logger;
import io.intino.tara.Language;
import io.quassar.editor.box.util.StringHelper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;


public class LanguageLoader {
	private final LanguageArtifactory artifactory;
	private final Map<String, Language> languageMap = new HashMap<>();

	public LanguageLoader(LanguageArtifactory artifactory) {
		this.artifactory = artifactory;
	}

	public Language get(String name) throws IOException {
		if (languageMap.containsKey(name)) return languageMap.get(name);
		File jarFile = artifactory.retrieve(name);
		Language language = load(name, jarFile);
		languageMap.put(name, language);
		return language;
	}

	private Language load(String dsl, File jar) throws IOException {
		try {
			String[] parts = dsl.split(":");
			final ClassLoader classLoader = createClassLoader(jar);
			if (classLoader == null) return null;
			Class<?> cls = classLoader.loadClass("tara.dsl." + firstUpperCase(StringHelper.kebabCaseToCamelCase(parts[0])));
			return (Language) cls.getConstructors()[0].newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			Logger.error(e);
			throw new IOException("Impossible to load language: " + e.getMessage(), e);
		}
	}

	private static ClassLoader createClassLoader(File jar) {
		try {
			return new URLClassLoader(new URL[]{jar.toURI().toURL()}, Language.class.getClassLoader());
		} catch (MalformedURLException e) {
			Logger.error(e.getMessage(), e);
			return null;
		}
	}

	private String firstUpperCase(String value) {
		return value.isEmpty() ? "" : value.substring(0, 1).toUpperCase() + value.substring(1);
	}
}
