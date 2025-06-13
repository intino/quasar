package io.quassar.editor.box.languages;

import io.intino.alexandria.logger.Logger;
import io.intino.tara.Language;
import io.quassar.editor.model.GavCoordinates;

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
	private final Map<GavCoordinates, Language> languageMap = new HashMap<>();

	public LanguageLoader(LanguageArtifactory artifactory) {
		this.artifactory = artifactory;
	}

	public Language get(GavCoordinates gav) throws IOException {
		if (languageMap.containsKey(gav)) return languageMap.get(gav);
		File jarFile = artifactory.retrieve(gav);
		Language language = load(gav, jarFile);
		languageMap.put(gav, language);
		return language;
	}

	public void remove(GavCoordinates gav) {
		languageMap.remove(gav);
	}

	private Language load(GavCoordinates gav, File jar) throws IOException {
		try {
			final ClassLoader classLoader = createClassLoader(jar);
			if (classLoader == null) return null;
			//Class<?> cls = classLoader.loadClass(gav.groupId() + "." + firstUpperCase(StringHelper.kebabCaseToCamelCase(gav.artifactId())));
			Class<?> cls = classLoader.loadClass(artifactory.mainClass(gav));
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
