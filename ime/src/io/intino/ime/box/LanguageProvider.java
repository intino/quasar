package io.intino.ime.box;

import io.intino.alexandria.logger.Logger;
import io.intino.tara.Language;
import io.intino.tara.builder.semantic.LanguageLoader;
import org.antlr.v4.runtime.misc.Pair;
import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static io.intino.itrules.formatters.StringFormatters.firstUpperCase;

public class LanguageProvider {
	public static final String LANGUAGES_PACKAGE = "tara/dsl";
	private final URL artifactory;
	private final Pair<String, String> credential;
	private final File dslsDirectory;
	private final Map<String, Language> languages = new HashMap<>();


	public LanguageProvider(File localRepository, URL artifactory) {
		this(localRepository, artifactory, null);
	}

	public LanguageProvider(File localRepository, URL artifactory, Pair<String, String> credential) {
		this.artifactory = artifactory;
		this.credential = credential;
		dslsDirectory = new File(localRepository, LANGUAGES_PACKAGE);
		dslsDirectory.mkdirs();
	}

	public Language get(String name) throws IOException {
		if (languages.containsKey(name)) return languages.get(name);
		File jarFile = download(name);
		Language language = load(name, jarFile);
		languages.put(name, language);
		return language;
	}

	private Language load(String dsl, File jar) throws IOException {
		try {
			String[] parts = dsl.split(":");
			final ClassLoader classLoader = createClassLoader(jar);
			if (classLoader == null) return null;
			Class<?> cls = classLoader.loadClass(LANGUAGES_PACKAGE.replace("/", ".") + "." + firstUpperCase().format(parts[0]).toString());
			return (Language) cls.getConstructors()[0].newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
				 InvocationTargetException e) {
			Logger.error(e);
			throw new IOException("Impossible to load language: " + e.getMessage(), e);
		}
	}

	private static ClassLoader createClassLoader(File jar) {
		try {
			return new URLClassLoader(new URL[]{jar.toURI().toURL()}, LanguageLoader.class.getClassLoader());
		} catch (MalformedURLException e) {
			Logger.error(e.getMessage(), e);
			return null;
		}
	}


	public File download(String dsl) throws IOException {
		String[] parts = dsl.split(":");
		String filename = parts[0] + "-" + parts[1] + ".jar";
		URL url = URI.create(String.join("/", artifactory.toString(), LANGUAGES_PACKAGE, parts[0], parts[1], filename)).toURL();
		File file = new File(dslsDirectory, filename);
		Files.write(file.toPath(), connect(url).readAllBytes());
		return file;
	}

	private InputStream connect(URL url) {
		try {
			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(2000);
			connection.setReadTimeout(2000);
			if (credential != null) {
				String auth = credential.a + ":" + credential.b;
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
}
