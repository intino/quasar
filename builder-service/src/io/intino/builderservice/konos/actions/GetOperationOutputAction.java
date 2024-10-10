package io.intino.builderservice.konos.actions;

import io.intino.alexandria.exceptions.AlexandriaException;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.exceptions.NotFound;
import io.intino.builder.CompilerMessage;
import io.intino.builderservice.konos.BuilderServiceBox;
import io.intino.builderservice.konos.runner.OperationOutputHandler;
import io.intino.builderservice.konos.runner.ProjectDirectory;
import io.intino.builderservice.konos.schemas.Message;
import io.intino.builderservice.konos.schemas.Message.Kind;
import io.intino.builderservice.konos.schemas.OperationResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static io.intino.builder.CompilerMessage.ERROR;
import static io.intino.builder.CompilerMessage.WARNING;
import static io.intino.builderservice.konos.schemas.OperationResult.State.Finished;
import static io.intino.builderservice.konos.schemas.OperationResult.State.Running;
import static java.util.Objects.requireNonNull;

public class GetOperationOutputAction implements io.intino.alexandria.rest.RequestErrorHandler {
	public BuilderServiceBox box;
	public io.intino.alexandria.http.spark.SparkContext context;
	public String ticket;

	public io.intino.builderservice.konos.schemas.OperationResult execute() throws NotFound {
		ProjectDirectory directory = ProjectDirectory.of(box.workspace(), ticket);
		if (!directory.exists()) throw new NotFound("Ticket does not exist");
		OperationResult result = new OperationResult();
		if (emptyIfNull(directory.gen().listFiles()).length > 0) result.genRef(directory.gen().getName());
		if (emptyIfNull(directory.src().listFiles()).length > 0) result.srcRef(directory.src().getName());
		if (emptyIfNull(directory.res().listFiles()).length > 0) result.resRef(directory.res().getName());
		if (emptyIfNull(directory.out().listFiles()).length > 0) result.outRef(directory.out().getName());
		OperationOutputHandler handler = box.operationHandler(ticket);
		result.messages(map(handler.compilerMessages()));
		result.state(box.containerManager().isRunning(ticket) ? Running : Finished);
		return result;
	}

	private List<Message> map(List<CompilerMessage> compilerMessages) {
		return new ArrayList<>(compilerMessages).stream()
				.map(c -> new Message().kind(kindOf(c.category())).column(c.columnNum()).content(c.message()).line(c.lineNum()).uri(c.url()))
				.toList();
	}

	private Kind kindOf(String category) {
		if (category.equals(ERROR)) return Kind.ERROR;
		if (category.equals(WARNING)) return Kind.WARNING;
		return Kind.INFO;
	}

	private File[] emptyIfNull(File[] files) {
		return files == null ? new File[0] : files;
	}

	public void onMalformedRequest(Throwable e) throws AlexandriaException {
		throw new BadRequest("Malformed request");
	}
}