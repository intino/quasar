package io.intino.builderservice.konos.actions;

import io.intino.alexandria.exceptions.AlexandriaException;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.exceptions.Conflict;
import io.intino.alexandria.exceptions.NotFound;
import io.intino.alexandria.logger.Logger;
import io.intino.builderservice.konos.BuilderServiceBox;
import io.intino.builderservice.konos.schemas.BuilderInfo;
import io.intino.builderservice.konos.utils.DockerManager;

import java.io.IOException;
import java.util.Optional;

public class GetBuilderInfoAction implements io.intino.alexandria.rest.RequestErrorHandler {
	public String registryToken;
	public BuilderServiceBox box;
	public io.intino.alexandria.http.spark.SparkContext context;
	public String imageURL;

	public BuilderInfo execute() throws Conflict, NotFound {
		Optional<BuilderInfo> info = box.builderStore().all().stream()
				.filter(b -> b.imageURL().equalsIgnoreCase(imageURL))
				.findFirst();
		if (info.isPresent()) return info.get();
		return findBuilder();
	}

	private BuilderInfo findBuilder() throws Conflict, NotFound {
		try {
			DockerManager.download(imageURL, registryToken);
			return DockerManager.builderInfo(imageURL);
		} catch (InterruptedException | IOException e) {
			Logger.error(e);
			throw new NotFound(e.getMessage());
		}
	}

	public void onMalformedRequest(Throwable e) throws AlexandriaException {
		//TODO
		throw new BadRequest("Malformed request");
	}
}