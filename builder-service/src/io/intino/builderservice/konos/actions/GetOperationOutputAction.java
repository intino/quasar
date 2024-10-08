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

import java.util.List;

import static io.intino.builderservice.konos.schemas.OperationResult.State.Finished;
import static io.intino.builderservice.konos.schemas.OperationResult.State.Running;

public class GetOperationOutputAction implements io.intino.alexandria.rest.RequestErrorHandler {
	public BuilderServiceBox box;
	public io.intino.alexandria.http.spark.SparkContext context;
	public String ticket;

	public io.intino.builderservice.konos.schemas.OperationResult execute() throws NotFound {
		ProjectDirectory directory = ProjectDirectory.of(box.workspace(), ticket);
		if (!directory.exists()) throw new NotFound("Ticket does not exist");
		OperationResult result = new OperationResult();
		result.genRef(directory.gen().getName());
		result.srcRef(directory.src().getName());
		result.resRef(directory.res().getName());
		result.graphRef(directory.out().getName() + "/graph.json");
		OperationOutputHandler handler = box.operationHandler(ticket);
		result.messages(map(handler.compilerMessages()));
		result.state(box.containerManager().isRunning(ticket) ? Running : Finished);
		return result;
	}

	private List<Message> map(List<CompilerMessage> compilerMessages) {
		return compilerMessages.stream()
				.map(c -> new Message().kind(kindOf(c.category())).column(c.columnNum()).content(c.message()).line(c.lineNum()).uri(c.url()))
				.toList();
	}

	private Message.Kind kindOf(String category) {
		if (category.equals(CompilerMessage.ERROR)) return Message.Kind.ERROR;
		if (category.equals(CompilerMessage.WARNING)) return Message.Kind.WARNING;
		return Message.Kind.INFO;
	}

	public void onMalformedRequest(Throwable e) throws AlexandriaException {
		//TODO
		throw new BadRequest("Malformed request");
	}
}