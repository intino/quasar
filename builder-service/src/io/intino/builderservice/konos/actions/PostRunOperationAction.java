package io.intino.builderservice.konos.actions;

import io.intino.alexandria.exceptions.AlexandriaException;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.alexandria.logger.Logger;
import io.intino.builderservice.konos.BuilderServiceBox;
import io.intino.builderservice.konos.runner.BuilderRunner;

import java.io.File;

public class PostRunOperationAction implements io.intino.alexandria.rest.RequestErrorHandler {
	public BuilderServiceBox box;
	public io.intino.alexandria.http.spark.SparkContext context;
	public io.intino.builderservice.konos.schemas.RunOperationContext runOperationContext;
	public io.intino.alexandria.Resource filesInTar;

	public String execute() throws InternalServerError {
		try {
			String ticket = new BuilderRunner(box.builderStore(), box.containerManager(), box.workspace(), new File(box.configuration().languageRepository()))
					.run(runOperationContext, filesInTar.inputStream());
			box.registerOperationHandler(ticket);
			return ticket;
		} catch (Throwable e) {
			Logger.error(e);
			throw new InternalServerError(e.getMessage());
		}
	}

	public void onMalformedRequest(Throwable e) throws AlexandriaException {
		throw new BadRequest("Malformed request");
	}
}