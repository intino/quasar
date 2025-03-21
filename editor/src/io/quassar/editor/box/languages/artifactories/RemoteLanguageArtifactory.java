package io.quassar.editor.box.languages.artifactories;

import io.intino.alexandria.logger.Logger;
import io.quassar.archetype.Archetype;
import io.quassar.editor.box.languages.LanguageArtifactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class RemoteLanguageArtifactory implements LanguageArtifactory {
	private final URL artifactory;
	private final Archetype archetype;
	private final Pair<String, String> credential;

	public RemoteLanguageArtifactory(URL artifactory, Archetype archetype, Pair<String, String> credential) {
		this.artifactory = artifactory;
		this.archetype = archetype;
		this.credential = credential;
	}

	@Override
	public File retrieve(String language) throws IOException {
		return download(language);
	}

	public File download(String dsl) throws IOException {
		String[] parts = dsl.split(":");
		String filename = parts[0] + "-" + parts[1].toLowerCase() + ".jar";
		File file = archetype.languages().dslFile(parts[0]);
		if (file.exists()) return file;
		URL url = URI.create(String.join("/", artifactory.toString(), "tara/dsl", parts[0], parts[1], filename)).toURL();
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

}
