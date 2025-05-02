package io.intino.builderservice.konos.actions;

import io.intino.builderservice.konos.BuilderServiceBox;
import io.intino.alexandria.exceptions.*;

public class PostBuildersAction implements io.intino.alexandria.rest.RequestErrorHandler {
	public io.intino.builderservice.konos.schemas.RegisterBuilder info;
	public BuilderServiceBox box;
	public io.intino.alexandria.http.server.AlexandriaHttpContext context;

	public void execute() {
		box.builderStore().put(info);
	}

	public void onMalformedRequest(Throwable e) throws AlexandriaException {
		throw new BadRequest("Malformed request");
	}
}