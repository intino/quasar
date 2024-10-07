package io.intino.builderservice.konos.actions;

import io.intino.builderservice.konos.BuilderServiceBox;
import io.intino.alexandria.exceptions.*;
import java.time.*;
import java.util.*;
import io.intino.builderservice.konos.schemas.*;

public class GetOutputAction implements io.intino.alexandria.rest.RequestErrorHandler {
	public BuilderServiceBox box;
	public io.intino.alexandria.http.spark.SparkContext context;
	public String ticket;

	public io.intino.builderservice.konos.schemas.OperationResult execute() {
		return null;
	}

	public void onMalformedRequest(Throwable e) throws AlexandriaException {
		//TODO
		throw new BadRequest("Malformed request");
	}
}