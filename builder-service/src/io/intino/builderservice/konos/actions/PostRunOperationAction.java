package io.intino.builderservice.konos.actions;

import io.intino.alexandria.exceptions.AlexandriaException;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.alexandria.exceptions.NotFound;
import io.intino.alexandria.logger.Logger;
import io.intino.builderservice.konos.BuilderServiceBox;
import io.intino.builderservice.konos.runner.BuilderRunner;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class PostRunOperationAction implements io.intino.alexandria.rest.RequestErrorHandler {
	public BuilderServiceBox box;
	public io.intino.alexandria.http.server.AlexandriaHttpContext context;
	public io.intino.builderservice.konos.schemas.RunOperationContext runOperationContext;
	public io.intino.alexandria.Resource filesInTar;

	public String execute() throws InternalServerError {
		try {
			if ( !new File(runOperationContext.languagePath()).exists()) throw new BadRequest("Language path does not exist");
			if (box.builderStore().get(runOperationContext.imageURL()) == null)
				throw new NotFound("Builder not found");
			if (filesInTar == null) throw new BadRequest("Required source files");
			copyLanguageToRepository();
			var ticketWithSources = new BuilderRunner(box.builderStore(), box.containerManager(), box.workspace(), new File(box.configuration().languageRepository()))
					.run(runOperationContext, filesInTar.inputStream());
			box.registerOperationHandler(ticketWithSources.getKey(), ticketWithSources.getValue());
			return ticketWithSources.getKey();
		} catch (Throwable e) {
			Logger.error(e);
			throw new InternalServerError(e.getMessage());
		}
	}

	private void copyLanguageToRepository() throws BadRequest, IOException {
		File repo = new File(box.configuration().languageRepository());
		File source = new File(runOperationContext.languagePath());
		if (!source.exists()) throw new BadRequest("Language file does not exist");
		repo.mkdirs();
		File destination = new File(repo, String.join(File.separator, runOperationContext.languageGroup().replace(".", File.separator),
				runOperationContext.languageName(),
				runOperationContext.languageVersion(),
				runOperationContext.languageName() + "-" + runOperationContext.languageVersion() + ".jar"));
		destination.getParentFile().mkdirs();
		if (!source.equals(destination)) FileUtils.copyFile(source, destination);
	}

	public void onMalformedRequest(Throwable e) throws AlexandriaException {
		throw new BadRequest("Malformed request");
	}
}