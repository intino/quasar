package io.intino.builderservice.konos.actions;

import io.intino.alexandria.exceptions.AlexandriaException;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.exceptions.NotFound;
import io.intino.builderservice.konos.BuilderServiceBox;
import io.intino.builderservice.konos.runner.ProjectDirectory;
import io.intino.builderservice.konos.schemas.OperationResult;
import io.intino.builderservice.konos.schemas.OperationResult.State;

import java.io.File;

public class GetOutputAction implements io.intino.alexandria.rest.RequestErrorHandler {
	public BuilderServiceBox box;
	public io.intino.alexandria.http.spark.SparkContext context;
	public String ticket;

	public OperationResult execute() throws NotFound {
		File projectDir = new File(box.workspace(), ticket);
		if (!projectDir.exists()) throw new NotFound("Ticket does not exist");
		OperationResult result = new OperationResult();
		ProjectDirectory directory = new ProjectDirectory(projectDir);
		result.genRef(directory.gen().getName());
		result.srcRef(directory.src().getName());
		result.resRef(directory.res().getName());
		result.graphRef(directory.out().getName() + "/graph.json");
		result.state(box.containerManager().isRunning(ticket) ? State.Running : State.Finished);
		return result;
	}

	public void onMalformedRequest(Throwable e) throws AlexandriaException {
		//TODO
		throw new BadRequest("Malformed request");
	}
}