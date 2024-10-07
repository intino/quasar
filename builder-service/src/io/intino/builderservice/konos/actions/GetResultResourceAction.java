package io.intino.builderservice.konos.actions;

import io.intino.builderservice.konos.BuilderServiceBox;
import io.intino.alexandria.exceptions.*;
import java.time.*;
import java.util.*;
import io.intino.builderservice.konos.schemas.*;

public class GetResultResourceAction implements io.intino.alexandria.rest.RequestErrorHandler {
	public String ticket;
	public String output;
	public BuilderServiceBox box;
	public io.intino.alexandria.http.spark.SparkContext context;
	public String resourceId;

	public io.intino.alexandria.Resource execute() throws NotFound {
		return null;
	}

	public void onMalformedRequest(Throwable e) throws AlexandriaException {
		//TODO
		throw new BadRequest("Malformed request");
	}
}