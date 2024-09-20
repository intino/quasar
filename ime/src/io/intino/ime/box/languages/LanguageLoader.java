package io.intino.ime.box.languages;

import io.intino.alexandria.logger.Logger;
import io.intino.tara.Language;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;


public class LanguageLoader {
	public static final String LANGUAGES_PACKAGE = "tara/dsl";
	private final URL artifactory;
	private final Pair<String, String> credential;
	private final File dslsDirectory;
	private final Map<String, Language> languageMap = new HashMap<>();


	public LanguageLoader(File localRepository, URL artifactory) {
		this(localRepository, artifactory, null);
	}

	public LanguageLoader(File localRepository, URL artifactory, Pair<String, String> credential) {
		this.artifactory = artifactory;
		this.credential = credential;
		dslsDirectory = new File(localRepository, LANGUAGES_PACKAGE);
		dslsDirectory.mkdirs();
	}

	public Language get(String name) throws IOException {
		if (languageMap.containsKey(name)) return languageMap.get(name);
		File jarFile = download(name);
		Language language = load(name, jarFile);
		languageMap.put(name, language);
		return language;
	}

	private Language load(String dsl, File jar) throws IOException {
		try {
			String[] parts = dsl.split(":");
			final ClassLoader classLoader = createClassLoader(jar);
			if (classLoader == null) return null;
			Class<?> cls = classLoader.loadClass(LANGUAGES_PACKAGE.replace("/", ".") + "." + firstUpperCase(parts[0]));
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

	public File download(String dsl) throws IOException {
		String[] parts = dsl.split(":");
		String filename = parts[0] + "-" + parts[1].toLowerCase() + ".jar";
		File file = new File(dslsDirectory, filename);
		if (file.exists()) return file;
		URL url = URI.create(String.join("/", artifactory.toString(), LANGUAGES_PACKAGE, parts[0], parts[1], filename)).toURL();
		Files.write(file.toPath(), connect(url).readAllBytes());
		return file;
	}

	private InputStream connect(URL url) {
		try {
			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(2000);
			connection.setReadTimeout(2000);
			if (credential != null) {
				String auth = credential.getKey() + ":" + credential.getValue();
				byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
				String authHeaderValue = "Basic " + new String(encodedAuth);
				connection.setRequestProperty("Authorization", authHeaderValue);
				return connection.getInputStream();
			}
			return openConnection(url);
		} catch (Throwable e) {
			Logger.error(e);
			return null;
		}
	}

	private InputStream openConnection(URL url) {
		try {
			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(2000);
			connection.setReadTimeout(2000);
			return connection.getInputStream();
		} catch (Throwable e) {
			return InputStream.nullInputStream();
		}
	}

	private String firstUpperCase(String value) {
		return value.isEmpty() ? "" : value.substring(0, 1).toUpperCase() + value.substring(1);
	}
}
