package io.intino.ime.box.orchestator;

import io.intino.alexandria.Resource;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.alexandria.exceptions.NotFound;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.Message;
import io.intino.builderservice.QuassarBuilderServiceAccessor;
import io.intino.builderservice.schemas.BuilderInfo;
import io.intino.builderservice.schemas.OperationResult;
import io.intino.builderservice.schemas.RunOperationContext;
import io.intino.ls.document.DocumentManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.intino.builderservice.schemas.OperationResult.State.Running;

public class BuilderOrchestator {

	public static final String QUASSAR_FILE = "quassar";
	private final DocumentManager manager;
	private final QuassarBuilderServiceAccessor accessor;
	private final QuassarParser quassar;
	private final List<String> builders;

	public BuilderOrchestator(URL builderedServiceUrl, DocumentManager manager) {
		this.manager = manager;
		this.accessor = new QuassarBuilderServiceAccessor(builderedServiceUrl);
		this.quassar = new QuassarParser(quassarContent());
		this.builders = getBuilders();
	}

	private List<String> getBuilders() {
		try {
			return accessor.getBuilders().stream().map(BuilderInfo::id).toList();
		} catch (InternalServerError e) {
			Logger.error(e);
			return Collections.emptyList();
		}
	}

	private String quassarContent() {
		try {
			return new String(manager.getDocumentText(new URI(QUASSAR_FILE)).readAllBytes());
		} catch (Exception e) {
			Logger.error(e);
			return "";
		}
	}

	public List<io.intino.builderservice.schemas.Message> build(String user) {
		try {
			File taraFiles = taraFiles();
			if (taraFiles == null) return new Message("Model files not found");
			runBuild(quassar.tara(), taraFiles); // TODO propagate errors of builders
			for (String b : builders()) runBuild(b.trim(), taraFiles);
			manager.commit(user);
			manager.push();
			return Collections.emptyList();
		} catch (Throwable t) {
			Logger.error(t);
			return new Message(t.getMessage()); // TODO error generico
		}
	}

	private void runBuild(String builder, File taraFiles) throws InternalServerError, IOException, NotFound, InterruptedException, URISyntaxException {
		String ticket = accessor.postRunOperation(context(builder).operation("Build"), Resource.InputStreamProvider.of(taraFiles));
		OperationResult output = accessor.getOperationOutput(ticket);
		while (output.state() == Running) {
			Thread.sleep(1000);
			output = accessor.getOperationOutput(ticket);
		}
		output.messages()
		doExtraction(ticket, output, quassar.pathOf(builder));
		moveGraphJson();
	}

	private void doExtraction(String ticket, OperationResult output, String builderPath) throws InternalServerError, NotFound, IOException {
		extractFiles(ticket, output.genRef(), builderPath + "/gen", true);
		extractFiles(ticket, output.srcRef(), builderPath + "/src", false);
		extractFiles(ticket, output.outRef(), builderPath + "/out", true);
		extractFiles(ticket, output.resRef(), builderPath + "/res", true);
	}

	private void moveGraphJson() throws URISyntaxException {
		URI old = manager.all().stream().filter(u -> u.getPath().endsWith("graph.json")).findFirst().orElse(null);
		if (old != null && !old.getPath().endsWith("graph/graph.json"))
			manager.move(old, new URI("graph/graph.json"));
	}

	private void extractFiles(String ticket, String ref, String path, boolean replace) throws InternalServerError, NotFound, IOException {
		if (ref == null || ref.isEmpty()) return;
		TarUtils.decompressTarFile(accessor.getOutputResource(ticket, ref, ".*\\." + langExtension() + "$").bytes(), manager, path, replace);
	}

	private List<String> builders() {
		return Arrays.asList(quassar.valueOf("builders").split(","));
	}

	private File taraFiles() {
		try {
			return TarUtils.createTarFile(manager, modelUris(), Files.createTempFile("quassar", ".tar").toFile());
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	private List<URI> modelUris() {
		return manager.all().stream().filter(l -> l.getPath().endsWith("." + langExtension())).toList();
	}

	private String langExtension() {
		return quassar.langName().toLowerCase();
	}

	private RunOperationContext context(String builder) {
		return new RunOperationContext()
				.builderId(imageOf(builder.split("@")[0]))
				.generationPackage(builder.contains("@") ? builder.split("@")[1] : "")
				.language(quassar.langQn())
				.languageVersion(quassar.langVersion())
				.project(quassar.projectName())
				.projectVersion(quassar.projectVersion());
	}

	private String imageOf(String builder) {
		return builders.stream().filter(b -> b.contains(builder)).findFirst().get();
	}

}
