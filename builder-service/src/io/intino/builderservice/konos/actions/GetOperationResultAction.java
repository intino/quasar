package io.intino.builderservice.konos.actions;

import io.intino.alexandria.exceptions.AlexandriaException;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.exceptions.NotFound;
import io.intino.builder.CompilerMessage;
import io.intino.builderservice.konos.BuilderServiceBox;
import io.intino.builderservice.konos.runner.OperationOutputHandler;
import io.intino.builderservice.konos.runner.ProjectDirectory;
import io.intino.builderservice.konos.schemas.Message;
import io.intino.builderservice.konos.schemas.OperationResult;

import java.util.ArrayList;
import java.util.List;

public class GetOperationResultAction implements io.intino.alexandria.rest.RequestErrorHandler {
	public BuilderServiceBox box;
	public io.intino.alexandria.http.spark.SparkContext context;
	public String ticket;

	public OperationResult execute() throws NotFound {
		ProjectDirectory directory = ProjectDirectory.of(box.workspace(), ticket);
		if (!directory.exists()) throw new NotFound("Ticket does not exist");
		OperationResult result = new OperationResult();
		result.genRef(directory.gen().getName());
		result.srcRef(directory.src().getName());
		result.resRef(directory.res().getName());
		result.graphRef(directory.out().getName() + "/graph.json");
		OperationOutputHandler handler = box.operationHandler(ticket);
		result.messages(map(handler.compilerMessages()));
		result.state(box.containerManager().isRunning(ticket) ? OperationResult.State.Running : OperationResult.State.Finished);
		return result;
	}

	private List<Message> map(List<CompilerMessage> compilerMessages) {
		List<Message> messages = new ArrayList<>();
		return messages;
	}

	public void onMalformedRequest(Throwable e) throws AlexandriaException {
		//TODO
		throw new BadRequest("Malformed request");
	}
}