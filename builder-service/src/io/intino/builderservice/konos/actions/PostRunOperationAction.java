package io.intino.builderservice.konos.actions;

import io.intino.alexandria.exceptions.AlexandriaException;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.alexandria.exceptions.NotFound;
import io.intino.alexandria.logger.Logger;
import io.intino.builderservice.konos.BuilderServiceBox;
import io.intino.builderservice.konos.runner.BuilderRunner;

import java.io.File;
import java.util.AbstractMap;
import java.util.List;

public class PostRunOperationAction implements io.intino.alexandria.rest.RequestErrorHandler {
	public BuilderServiceBox box;
	public io.intino.alexandria.http.server.AlexandriaHttpContext context;
	public io.intino.builderservice.konos.schemas.RunOperationContext runOperationContext;
	public io.intino.alexandria.Resource filesInTar;

	public String execute() throws InternalServerError {
		try {
			if (box.builderStore().get(runOperationContext.imageURL()) == null)
				throw new NotFound("Builder not found");
			if (filesInTar == null) throw new BadRequest("Required source files");
			var ticketWithSources = new BuilderRunner(box.builderStore(), box.containerManager(), box.workspace(), new File(box.configuration().languageRepository()))
					.run(runOperationContext, filesInTar.inputStream());
			box.registerOperationHandler(ticketWithSources.getKey(), ticketWithSources.getValue());
			return ticketWithSources.getKey();
		} catch (Throwable e) {
			Logger.error(e);
			throw new InternalServerError(e.getMessage());
		}
	}

	public void onMalformedRequest(Throwable e) throws AlexandriaException {
		throw new BadRequest("Malformed request");
	}
}