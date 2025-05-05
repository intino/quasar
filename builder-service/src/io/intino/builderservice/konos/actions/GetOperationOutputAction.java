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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.intino.builderservice.konos.schemas.OperationResult.State.Finished;
import static io.intino.builderservice.konos.schemas.OperationResult.State.Running;

public class GetOperationOutputAction implements io.intino.alexandria.rest.RequestErrorHandler {
	public BuilderServiceBox box;
	public io.intino.alexandria.http.server.AlexandriaHttpContext context;
	public String ticket;

	public io.intino.builderservice.konos.schemas.OperationResult execute() throws NotFound {
		ProjectDirectory directory = ProjectDirectory.of(box.workspace(), ticket);
		if (!directory.exists()) throw new NotFound("Ticket does not exist");
		OperationOutputHandler handler = box.operationHandler(ticket);
		handler.readOutput();
		OperationResult result = new OperationResult();
		if (emptyIfNull(directory.gen().listFiles()).length > 0) result.genRef(directory.gen().getName());
		if (srcFiles(directory, handler).length > 0) result.srcRef(directory.src().getName());
		if (emptyIfNull(directory.res().listFiles()).length > 0) result.resRef(directory.res().getName());
		if (emptyIfNull(directory.out().listFiles()).length > 0) result.outRef(directory.out().getName());
		if (emptyIfNull(directory.build().listFiles()).length > 0) result.buildRef(directory.build().getName());
		result.messages(map(handler.compilerMessages(), directory));
		result.success(result.messages().stream().noneMatch(m -> m.kind().equals(Kind.ERROR)));
		result.state(box.containerManager().isRunning(ticket) ? Running : Finished);
		return result;
	}

	private File[] srcFiles(ProjectDirectory directory, OperationOutputHandler handler) {
		return excludeSourceFiles(directory.src(), handler.srcFiles());
	}

	private List<Message> map(List<CompilerMessage> compilerMessages, ProjectDirectory directory) {
		return new ArrayList<>(compilerMessages).stream()
				.map(c -> messageOf(directory, c))
				.toList();
	}

	private Message messageOf(ProjectDirectory directory, CompilerMessage c) {
		return new Message()
				.kind(kindOf(c.category())).
				column(c.columnNum())
				.content(c.message())
				.line(c.lineNum())
				.uri(c.url().replace(canonicalPath(directory), ""));
	}

	private File[] excludeSourceFiles(File srcDirectory, List<File> srcFiles) {
		return FileUtils.listFiles(srcDirectory, new IOFileFilter() {
			@Override
			public boolean accept(File file) {
				return !srcFiles.contains(file);
			}

			@Override
			public boolean accept(File dir, String name) {
				return true;
			}
		}, TrueFileFilter.INSTANCE).toArray(new File[0]);
	}

	private Kind kindOf(String category) {
		if (category.equalsIgnoreCase(Kind.ERROR.name())) return Kind.ERROR;
		if (category.equalsIgnoreCase(Kind.WARNING.name())) return Kind.WARNING;
		return Kind.INFO;
	}

	private File[] emptyIfNull(File[] files) {
		return files == null ? new File[0] : files;
	}

	public void onMalformedRequest(Throwable e) throws AlexandriaException {
		throw new BadRequest("Malformed request");
	}

	private static String canonicalPath(ProjectDirectory directory) {
		try {
			return directory.src().getCanonicalPath() + File.separator;
		} catch (IOException e) {
			return "";
		}
	}
}