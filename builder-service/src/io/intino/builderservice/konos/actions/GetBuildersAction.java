package io.intino.builderservice.konos.actions;

import io.intino.alexandria.exceptions.AlexandriaException;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.builderservice.konos.BuilderServiceBox;

import java.util.ArrayList;
import java.util.List;

public class GetBuildersAction implements io.intino.alexandria.rest.RequestErrorHandler {
	public BuilderServiceBox box;
	public io.intino.alexandria.http.server.AlexandriaHttpContext context;

	public List<io.intino.builderservice.konos.schemas.BuilderInfo> execute() {
		return new ArrayList<>(box.builderStore().all());
	}

	public void onMalformedRequest(Throwable e) throws AlexandriaException {
		throw new BadRequest("Malformed request");
	}
}