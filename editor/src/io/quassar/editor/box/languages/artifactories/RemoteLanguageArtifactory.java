package io.quassar.editor.box.languages.artifactories;

import io.intino.alexandria.logger.Logger;
import io.quassar.archetype.Archetype;
import io.quassar.editor.box.languages.LanguageArtifactory;
import io.quassar.editor.box.languages.LanguageManager;
import io.quassar.editor.box.languages.MetamodelProvider;
import io.quassar.editor.box.util.ArchetypeHelper;
import io.quassar.editor.box.util.Formatters;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.box.util.StringHelper;
import io.quassar.editor.model.GavCoordinates;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;
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
	private final MetamodelProvider metamodelProvider;
	private final Pair<String, String> credential;

	public RemoteLanguageArtifactory(URL artifactory, Archetype archetype, MetamodelProvider provider, Pair<String, String> credential) {
		this.artifactory = artifactory;
		this.archetype = archetype;
		this.metamodelProvider = provider;
		this.credential = credential;
	}

	@Override
	public File retrieve(GavCoordinates gav) throws IOException {
		return download(gav);
	}

	@Override
	public String mainClass(GavCoordinates gav) {
		Model model = metamodelProvider.provide(gav.languageId());
		if (model == null) return gav.groupId() + "." + Formatters.firstUpperCase(StringHelper.kebabCaseToCamelCase(gav.artifactId()));
		return LanguageHelper.TaraDslPackage + Formatters.firstUpperCase(StringHelper.kebabCaseToCamelCase(model.name()));
	}

	public File download(GavCoordinates gav) throws IOException {
		String filename = gav.artifactId() + "-" + gav.version().toLowerCase() + ".jar";
		File file = archetype.languages().releaseDsl(ArchetypeHelper.languageDirectoryName(gav.languageId()), gav.version());
		if (file.exists()) return file;
		URL url = URI.create(String.join("/", artifactory.toString(), gav.groupId(), gav.artifactId(), gav.version(), filename)).toURL();
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
