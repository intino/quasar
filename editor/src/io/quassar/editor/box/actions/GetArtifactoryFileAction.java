package io.quassar.editor.box.actions;

import io.intino.alexandria.Resource;
import io.intino.alexandria.exceptions.AlexandriaException;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.exceptions.Forbidden;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.util.ArtifactoryHelper;
import io.quassar.editor.box.util.ZipHelper;
import io.quassar.editor.model.GavCoordinates;
import io.quassar.editor.model.Language;

import java.util.Arrays;
import java.util.stream.Collectors;

public class GetArtifactoryFileAction implements io.intino.alexandria.rest.RequestErrorHandler {
	public EditorBox box;
	public io.intino.alexandria.http.server.AlexandriaHttpContext context;

	public io.intino.alexandria.Resource execute() throws Forbidden {
		String groupId = ArtifactoryHelper.groupId(pathInfo());
		String artifactId = ArtifactoryHelper.artifactId(pathInfo());
		String version = ArtifactoryHelper.version(pathInfo());
		String file = ArtifactoryHelper.file(pathInfo());

		if (groupId == null || artifactId == null || version == null || file == null)
			throw new Forbidden("Repository file not found");

		String extension = fileExtension();
		Language language = box.languageManager().get(new GavCoordinates(!groupId.equals(Language.QuassarGroup) ? groupId : "", artifactId, version));
		boolean isJar = extension.equals(".jar");
		boolean isManifest = extension.equals(".pom");

		if (file.equals("graph")) return isJar ? new Resource(box.languageManager().loadGraph(language, language.release(version))) : null;
		if (file.equals("dsl")) return isJar ? new Resource(box.languageManager().loadDsl(language, language.release(version))) : null;

		if (isManifest) return new Resource(box.languageManager().loadReaderManifest(language, language.release(version), file));
		return new Resource(box.languageManager().loadReader(language, language.release(version), file));
	}

	private String fileExtension() {
		return pathInfo().substring(pathInfo().lastIndexOf("."));
	}

	private String pathInfo() {
		String pathInfo = context.get("pathInfo");
		String defaultPath = "/artifactory/releases";
		return pathInfo.substring(pathInfo.indexOf(defaultPath) + defaultPath.length() + 1);
	}

	public void onMalformedRequest(Throwable e) throws AlexandriaException {
		//TODO
		throw new BadRequest("Malformed request");
	}
}