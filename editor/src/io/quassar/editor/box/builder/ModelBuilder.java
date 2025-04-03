package io.quassar.editor.box.builder;

import io.intino.alexandria.Resource;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.alexandria.exceptions.NotFound;
import io.intino.alexandria.logger.Logger;
import io.intino.builderservice.QuassarBuilderServiceAccessor;
import io.intino.builderservice.schemas.Message;
import io.intino.builderservice.schemas.OperationResult;
import io.intino.builderservice.schemas.RunOperationContext;
import io.intino.ls.document.DocumentManager;
import io.intino.ls.document.FileDocumentManager;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.util.Formatters;
import io.quassar.editor.box.util.TarUtils;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;

import static io.intino.builderservice.schemas.OperationResult.State.Running;

public class ModelBuilder {

	private final Model model;
	private final String release;
	private final Language language;
	private final DocumentManager manager;
	private final QuassarBuilderServiceAccessor accessor;
	private final String quassarBuilder;

	public ModelBuilder(Model model, String release, EditorBox box) throws IOException {
		this.model = model;
		this.release = release;
		this.language = box.languageManager().get(model.language());
		this.manager = new FileDocumentManager(box.modelManager().workspace(model, Model.DraftRelease).root());
		this.accessor = box.builderAccessor();
		this.quassarBuilder = box.configuration().quassarBuilder();
	}

	public BuildResult build(String user) {
		try {
			File taraFiles = taraFiles();
			if (taraFiles == null)
				return BuildResult.failure(new Message().kind(Message.Kind.ERROR).content("Model files not found"));
			BuildResult result = doBuild(taraFiles);
			if (!result.success()) return result;
			manager.commit(user);
			manager.push();
			return result;
		} catch (Throwable t) {
			Logger.error(t);
			return BuildResult.failure(new Message().kind(Message.Kind.ERROR).content("Unknown error"));
		}
	}

	private BuildResult doBuild(File taraFiles) throws InternalServerError, IOException, NotFound, InterruptedException, URISyntaxException {
		return runBuild(quassarBuilder, taraFiles, "Build");
	}

	private BuildResult runBuild(String builder, File taraFiles, String operation) throws InternalServerError, IOException, NotFound, InterruptedException, URISyntaxException {
		String ticket = accessor.postRunOperation(context(builder).operation(operation), Resource.InputStreamProvider.of(taraFiles));
		OperationResult output = accessor.getOperationOutput(ticket);
		while (output.state() == Running) {
			Thread.sleep(1000);
			output = accessor.getOperationOutput(ticket);
		}
		if (!output.success()) return BuildResult.failure(output.messages());
		Resource resource = output.outRef() != null ? accessor.getOutputResource(ticket, output.outRef(), null) : null;
		return BuildResult.success(resource != null ? resource.inputStream() : new ByteArrayInputStream(new byte[0]));
	}

	private File taraFiles() {
		try {
			List<URI> uris = modelUris();
			if (uris.isEmpty()) return null;
			return TarUtils.createTarFile(manager, uris, Files.createTempFile("quassar", ".tar").toFile());
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	private List<URI> modelUris() {
		return manager.all().stream().filter(l -> l.getPath().endsWith("." + langExtension())).toList();
	}

	private String langExtension() {
		return language.fileExtension().toLowerCase();
	}

	private RunOperationContext context(String builder) {
		return new RunOperationContext()
				.imageURL(builder)
				.generationPackage("")
				.language(Formatters.normalizeLanguageName(language.name()))
				.languageVersion(language.version())
				.project(Formatters.normalizeLanguageName(model.name()))
				.projectVersion(release);
	}

}
