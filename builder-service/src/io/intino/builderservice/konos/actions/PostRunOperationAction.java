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
			return new BuilderRunner(box.builderStore(), box.workspace(), box.configuration().dockerhubAuthFile(), new File(box.configuration().languageRepository()))
					.run(runOperationContext, filesInTar.inputStream());
		} catch (Throwable e) {
			Logger.error(e);
			throw new InternalServerError(e.getMessage());
		}
	}

	public void onMalformedRequest(Throwable e) throws AlexandriaException {
		//TODO
		throw new BadRequest("Malformed request");
	}
}