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
import io.quassar.editor.box.util.TarUtils;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Collections;
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
		this.manager = new FileDocumentManager(new File(box.modelManager().workspace(model, Model.DraftRelease)));
		this.accessor = box.builderAccessor();
		this.quassarBuilder = box.configuration().quassarBuilder();
	}

	public List<Message> build(String user) {
		try {
			File taraFiles = taraFiles();
			if (taraFiles == null)
				return List.of(new Message().kind(Message.Kind.ERROR).content("Model files not found"));
			List<Message> messages = doBuild(taraFiles);
			if (!messages.isEmpty()) return messages;
			manager.commit(user);
			manager.push();
		} catch (Throwable t) {
			Logger.error(t);
			return List.of(new Message().kind(Message.Kind.ERROR).content("Unknown error"));
		}
		return Collections.emptyList();
	}

	private List<Message> doBuild(File taraFiles) throws InternalServerError, IOException, NotFound, InterruptedException, URISyntaxException {
		List<Message> messages = runBuild(quassarBuilder, taraFiles, "Build");
		return !messages.isEmpty() ? messages : Collections.emptyList();
	}

	private List<Message> runBuild(String builder, File taraFiles, String operation) throws InternalServerError, IOException, NotFound, InterruptedException, URISyntaxException {
		String ticket = accessor.postRunOperation(context(builder).operation(operation), Resource.InputStreamProvider.of(taraFiles));
		OperationResult output = accessor.getOperationOutput(ticket);
		while (output.state() == Running) {
			Thread.sleep(1000);
			output = accessor.getOperationOutput(ticket);
		}
		if (!output.success()) return output.messages();
		return Collections.emptyList();
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
				.language(language.name())
				.languageVersion(language.version())
				.project(model.name())
				.projectVersion(release);
	}

}
