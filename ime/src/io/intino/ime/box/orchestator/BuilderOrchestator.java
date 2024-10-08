package io.intino.ime.box.orchestator;

import io.intino.alexandria.Resource;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.alexandria.exceptions.NotFound;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.Message;
import io.intino.builderservice.QuassarBuilderServiceAccessor;
import io.intino.builderservice.schemas.OperationResult;
import io.intino.builderservice.schemas.RunOperationContext;
import io.intino.ime.box.ImeBox;
import io.intino.ls.document.DocumentManager;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static io.intino.builderservice.schemas.OperationResult.State.Running;

public class BuilderOrchestator {

	private final DocumentManager manager;
	private final QuassarBuilderServiceAccessor accessor;
	private final QuassarParser quassar;

	public BuilderOrchestator(ImeBox box, DocumentManager manager) {
		this.manager = manager;
		this.accessor = new QuassarBuilderServiceAccessor(builderServiceUrl(box));
		this.quassar = new QuassarParser(quassarContent());
	}

	private static URI quassarUri() {
		try {
			return new URI("./.quassar");
		} catch (URISyntaxException e) {
			Logger.error(e);
			return null;
		}
	}

	private static URL builderServiceUrl(ImeBox box) {
		try {
			return new URI(box.configuration().builderServiceUrl()).toURL();
		} catch (MalformedURLException | URISyntaxException e) {
			Logger.error(e);
			return null;
		}
	}

	private String quassarContent() {
		try {
			return new String(manager.getDocumentText(quassarUri()).readAllBytes());
		} catch (IOException e) {
			Logger.error(e);
			return "";
		}
	}

	public Message build() {
		File taraFiles = taraFiles();
		if (taraFiles == null) return new Message("Model files not found");
		runBuild(quassar.tara(), taraFiles);
		for (String b : builders()) runBuild(b.trim(), taraFiles);
		manager.commit();
		return new Message("OK");
	}

	private void runBuild(String builder, File taraFiles) {
		try {
			String ticket = accessor.postRunOperation(context(builder).operation("build"), new Resource(taraFiles));
			OperationResult output = accessor.getOperationOutput(ticket);
			while (output.state() == Running) {
				Thread.sleep(1000);
				output = accessor.getOperationOutput(ticket);
			}
			extractFiles(ticket, output.genRef(), quassar.pathOf(builder) + "/gen", true);
			extractFiles(ticket, output.srcRef(), quassar.pathOf(builder) + "/src", false);
			extractFiles(ticket, output.graphRef(), quassar.pathOf(builder, "graph") + "/graph", true);
			extractFiles(ticket, output.resRef(), quassar.pathOf(builder) + "/res", true);
		} catch (Throwable e) {
			Logger.error(e);
		}
	}

	private void extractFiles(String ticket, String ref, String path, boolean replace) throws InternalServerError, NotFound, IOException {
		if(ref == null || ref.isEmpty()) return;
		TarUtils.decompressTarFile(accessor.getOutputResource(ticket, ref).bytes(), manager, path, replace);
	}

	private List<String> builders() {
		return Arrays.asList(quassar.valueOf("builders").split(","));
	}

	private File taraFiles() {
		try {
			return TarUtils.createTarFile(modelUris(), Files.createTempFile("quassar", ".tar").toFile());
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	private List<URI> modelUris() {
		return manager.all().stream().filter(l -> l.getPath().endsWith("." + quassar)).toList();
	}

	private RunOperationContext context(String builder) {
		return new RunOperationContext()
				.builderId(builder.split("@")[0])
				.generationPackage(builder.contains("@") ? builder.split("@")[1] : "")
				.language(quassar.langName())
				.languageVersion(quassar.langVersion())
				.project(quassar.projectName())
				.projectVersion(quassar.projectVersion());
	}

}
