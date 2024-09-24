package io.intino.builderservice.konos.actions;

import io.intino.builderservice.konos.BuilderServiceBox;
import io.intino.alexandria.exceptions.*;
import java.time.*;
import java.util.*;
import io.intino.builderservice.konos.schemas.*;

public class PostBuildersAction implements io.intino.alexandria.rest.RequestErrorHandler {
	public BuilderInfo builderInfo;
	public BuilderServiceBox box;
	public io.intino.alexandria.http.spark.SparkContext context;

	public void execute() {

	}

	public void onMalformedRequest(Throwable e) throws AlexandriaException {
		//TODO
		throw new BadRequest("Malformed request");
	}
}