package io.intino.builderservice.konos.actions;

import io.intino.alexandria.exceptions.AlexandriaException;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.exceptions.Conflict;
import io.intino.alexandria.exceptions.NotFound;
import io.intino.alexandria.logger.Logger;
import io.intino.builderservice.konos.BuilderServiceBox;
import io.intino.builderservice.konos.schemas.BuilderInfo;

import java.io.IOException;
import java.util.Optional;

public class GetBuilderInfoAction implements io.intino.alexandria.rest.RequestErrorHandler {
	public String registryToken;
	public BuilderServiceBox box;
	public io.intino.alexandria.http.server.AlexandriaHttpContext context;
	public String imageURL;

	public BuilderInfo execute() throws Conflict, NotFound {
		Optional<BuilderInfo> info = box.builderStore().all().stream()
				.filter(b -> b.imageURL().equalsIgnoreCase(imageURL))
				.findFirst();
		return info.isPresent() ? info.get() : findBuilder();
	}

	private BuilderInfo findBuilder() throws Conflict, NotFound {
		try {
			box.containerManager().download(imageURL, registryToken);
			return box.containerManager().builderInfo(imageURL);
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