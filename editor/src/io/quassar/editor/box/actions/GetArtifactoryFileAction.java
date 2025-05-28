package io.quassar.editor.box.actions;

import io.intino.alexandria.Resource;
import io.intino.alexandria.exceptions.AlexandriaException;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.exceptions.Forbidden;
import io.intino.alexandria.exceptions.NotFound;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.util.ArtifactoryHelper;
import io.quassar.editor.model.GavCoordinates;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;

import java.io.File;

public class GetArtifactoryFileAction implements io.intino.alexandria.rest.RequestErrorHandler {
	public EditorBox box;
	public io.intino.alexandria.http.server.AlexandriaHttpContext context;

	public io.intino.alexandria.Resource execute() throws Forbidden, NotFound {
		GavCoordinates coordinates = ArtifactoryHelper.parse(pathInfo());

		if (coordinates == null)
			throw new Forbidden("Repository file not found");

		Language language = locateLanguage(coordinates);
		if (language == null)
			throw new NotFound("Repository file not found");

		LanguageRelease release = language.release(coordinates.version());
		if (release == null)
			throw new NotFound("Repository file not found");

		String extension = fileExtension();
		boolean isJar = extension.equals(".jar");
		boolean isManifest = extension.equals(".pom");

		if (coordinates.artifactId().equals("graph")) return isJar ? new Resource(box.languageManager().loadGraph(language, release)) : emptyManifest();

		boolean isDsl = coordinates.artifactId().equals(language.name());
		return isDsl ? loadDsl(language, release, isManifest) : loadReader(language, release, coordinates.artifactId(), isManifest);
	}

	private Language locateLanguage(GavCoordinates coordinates) {
		Language language = box.languageManager().get(coordinates);
		if (language == null) language = box.languageManager().get(Language.nameFrom(coordinates.groupId()));
		return language;
	}

	private Resource loadDsl(Language language, LanguageRelease release, boolean isManifest) {
		if (isManifest) {
			File result = box.languageManager().loadDslManifest(language, release);
			return result != null ? new Resource(result) : emptyManifest();
		}
		return new Resource(box.languageManager().loadDsl(language, release));
	}

	private Resource loadReader(Language language, LanguageRelease release, String artifactId, boolean isManifest) {
		String file = ArtifactoryHelper.readerNameFrom(artifactId);
		if (isManifest) return new Resource(box.languageManager().loadReaderManifest(language, release, file));
		return new Resource(box.languageManager().loadReader(language, release, file));
	}

	private String fileExtension() {
		return pathInfo().substring(pathInfo().lastIndexOf("."));
	}

	private String pathInfo() {
		String pathInfo = context.get("pathInfo");
		String defaultPath = ArtifactoryHelper.ReleasesPath;
		return pathInfo.substring(pathInfo.indexOf(defaultPath) + defaultPath.length() + 1);
	}

	public void onMalformedRequest(Throwable e) throws AlexandriaException {
		//TODO
		throw new BadRequest("Malformed request");
	}

	private static Resource emptyManifest() {
		return new Resource("pom.xml", new byte[0]);
	}

}