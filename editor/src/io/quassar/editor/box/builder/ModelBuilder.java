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
import io.quassar.editor.model.GavCoordinates;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;

import static io.intino.builderservice.QuassarBuilderServiceAccessor.OutputResourceOutput.Build;
import static io.intino.builderservice.schemas.OperationResult.State.Running;

public class ModelBuilder {
	private final Model model;
	private final GavCoordinates destination;
	private final Language language;
	private final DocumentManager manager;
	private final QuassarBuilderServiceAccessor accessor;
	private final String quassarBuilder;

	public ModelBuilder(Model model, GavCoordinates destination, EditorBox box) throws IOException {
		this.model = model;
		this.destination = destination;
		this.language = box.languageManager().get(model.language());
		this.manager = new FileDocumentManager(box.modelManager().workspace(model, Model.DraftRelease).root());
		this.accessor = box.builderAccessor();
		this.quassarBuilder = box.configuration().quassarBuilder();
	}

	public CheckResult check(String user) {
		File taraFiles = null;
		try {
			taraFiles = taraFiles();
			if (taraFiles == null)
				return CheckResult.failure(new Message().kind(Message.Kind.ERROR).content("Model files not found"));
			return doCheck(taraFiles);
		} catch (Throwable t) {
			Logger.error(t);
			return CheckResult.failure(new Message().kind(Message.Kind.ERROR).content("Unknown error"));
		} finally {
			if (taraFiles != null) taraFiles.delete();
		}
	}

	public BuildResult build(String user) throws InternalServerError, NotFound {
		CheckResult result = check(user);
		if (!result.success()) return BuildResult.failure(result.messages());
		manager.commit(user);
		manager.push();
		return BuildResult.success(result.messages(), artifacts(result));
	}

	private Resource artifacts(CheckResult result) throws InternalServerError, NotFound {
		OperationResult output = accessor.getOperationOutput(result.ticket());
		return output.buildRef() != null ? accessor.getOutputResource(result.ticket(), Build, null) : null;
	}

	private CheckResult doCheck(File taraFiles) throws InternalServerError, IOException, NotFound, InterruptedException, URISyntaxException {
		return runCheck(quassarBuilder, taraFiles, "Build");
	}

	private CheckResult runCheck(String builder, File taraFiles, String operation) throws InternalServerError, IOException, NotFound, InterruptedException, URISyntaxException {
		String ticket = accessor.postRunOperation(context(builder).operation(operation), Resource.InputStreamProvider.of(taraFiles));
		OperationResult output = accessor.getOperationOutput(ticket);
		while (output.state() == Running) {
			Thread.sleep(1000);
			output = accessor.getOperationOutput(ticket);
		}
		return output.success() ? CheckResult.success(ticket) : CheckResult.failure(ticket, output.messages());
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
		return manager.all().stream().filter(l -> l.getPath().endsWith(langExtension())).toList();
	}

	private String langExtension() {
		return Language.FileExtension.toLowerCase();
	}

	private RunOperationContext context(String builder) {
		return new RunOperationContext()
				.imageURL(builder)
				.generationPackage("")
				.language(Formatters.normalizeLanguageName(language.name()))
				.languageVersion(model.language().version())
				.project(Formatters.normalizeLanguageName(destination.artifactId()))
				.projectVersion(destination.version());
	}

}
