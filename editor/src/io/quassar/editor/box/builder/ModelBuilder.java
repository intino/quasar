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
	private final File languagePath;
	private final DocumentManager manager;
	private final QuassarBuilderServiceAccessor accessor;
	private final String quassarBuilder;

	private static final String CheckOperation = "Check";
	private static final String BuildOperation = "Build";

	public ModelBuilder(Model model, String release, GavCoordinates destination, EditorBox box) throws IOException {
		this.model = model;
		this.destination = destination;
		this.language = box.languageManager().get(model.language());
		this.languagePath = box.languageManager().loadDsl(model.language());
		this.quassarBuilder = box.configuration().quassarBuilder();
		this.manager = new FileDocumentManager(box.modelManager().workspace(model, release).root());
		this.accessor = box.builderAccessor();
	}

	public CheckResult check(String user) {
		return run(CheckOperation);
	}

	public BuildResult build(String user) throws InternalServerError, NotFound {
		CheckResult result = run(BuildOperation);
		if (!result.success()) return BuildResult.failure(result.messages());
		manager.commit(user);
		manager.push();
		return BuildResult.success(result.messages(), artifacts(result));
	}

	private Resource artifacts(CheckResult result) throws InternalServerError, NotFound {
		OperationResult output = accessor.getOperationOutput(result.ticket());
		return output.buildRef() != null ? accessor.getOutputResource(result.ticket(), Build, null) : null;
	}

	private CheckResult run(String operation) {
		File taraFiles = null;
		try {
			taraFiles = taraFiles();
			if (taraFiles == null)
				return CheckResult.failure(new Message().kind(Message.Kind.ERROR).content("Model files not found"));
			return doRun(taraFiles, operation);
		} catch (Throwable t) {
			Logger.error(t);
			return CheckResult.failure(new Message().kind(Message.Kind.ERROR).content("Unknown error"));
		} finally {
			if (taraFiles != null) taraFiles.delete();
		}
	}

	private CheckResult doRun(File taraFiles, String operation) throws InternalServerError, IOException, NotFound, InterruptedException, URISyntaxException {
		String ticket = accessor.postRunOperation(context(quassarBuilder).operation(operation), Resource.InputStreamProvider.of(taraFiles));
		OperationResult output = accessor.getOperationOutput(ticket);
		while (output.state() == Running) {
			Thread.sleep(1000);
			output = accessor.getOperationOutput(ticket);
		}
		return output.success() ? CheckResult.success(ticket, output.messages()) : CheckResult.failure(ticket, output.messages());
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
				.generationPackage(Language.QuassarGroup + "." + destination.groupId() + "." + Formatters.normalizeLanguageName(destination.artifactId()).toLowerCase())
				.languageGroup(language.isFoundational() ? Language.TaraGroup : Language.QuassarGroup + "." + language.collection())
				.languageName(Formatters.normalizeLanguageName(language.name()))
				.languageVersion(model.language().version())
				.languagePath(languagePath.getAbsolutePath())
				.projectGroup(Language.QuassarGroup + "." + destination.groupId())
				.projectName(Formatters.normalizeLanguageName(destination.artifactId()))
				.projectVersion(destination.version());
	}

}
