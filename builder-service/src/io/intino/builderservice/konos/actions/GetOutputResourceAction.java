package io.intino.builderservice.konos.actions;

import io.intino.alexandria.Resource;
import io.intino.alexandria.exceptions.AlexandriaException;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.alexandria.exceptions.NotFound;
import io.intino.alexandria.logger.Logger;
import io.intino.builderservice.konos.BuilderServiceBox;
import io.intino.builderservice.konos.rest.resources.GetOutputResourceResource;
import io.intino.builderservice.konos.runner.ProjectDirectory;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;

import static io.intino.builderservice.konos.utils.Tar.tar;

public class GetOutputResourceAction implements io.intino.alexandria.rest.RequestErrorHandler {
	public GetOutputResourceResource.Output output;
	public BuilderServiceBox box;
	public io.intino.alexandria.http.server.AlexandriaHttpContext context;
	public String ticket;
	public String excludeFilePattern;

	public io.intino.alexandria.Resource execute() throws NotFound, InternalServerError {
		ProjectDirectory directory = ProjectDirectory.of(box.workspace(), ticket);
		if (!directory.exists()) throw new NotFound("Ticket does not exist");
		File file = tempFile();
		FileFilter filter = excludeFilePattern != null ? new NotFileFilter(new RegexFileFilter(excludeFilePattern)) : f -> true;
		try {
			switch (output) {
				case Gen -> {
					if (emptyIfNull(directory.gen().listFiles()).length == 0) throw new NotFound("Directory is empty");
					tar(directory.gen(), filter, file);
				}
				case Src -> {
					if (emptyIfNull(directory.src().listFiles()).length == 0) throw new NotFound("Directory is empty");
					tar(directory.src(), filter, file);
				}
				case Res -> {
					if (emptyIfNull(directory.res().listFiles()).length == 0) throw new NotFound("Directory is empty");
					tar(directory.res(), filter, file);
				}
				case Out -> {
					if (emptyIfNull(directory.out().listFiles()).length == 0) throw new NotFound("Directory is empty");
					tar(directory.out(), filter, file);
				}
				case Build -> {
					if (emptyIfNull(directory.build().listFiles()).length == 0)
						throw new NotFound("Directory is empty");
					tar(directory.build(), filter, file);
				}
				default -> throw new NotFound("Output not found: " + output);
			}
			return new Resource("file", file);
		} catch (IOException e) {
			throw new InternalServerError(e.getMessage());
		}
	}

	private File[] emptyIfNull(File[] files) {
		return files == null ? new File[0] : files;
	}

	private static File tempFile() {
		try {
			return Files.createTempFile("output", ".tar").toFile();
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	public void onMalformedRequest(Throwable e) throws AlexandriaException {
		//TODO
		throw new BadRequest("Malformed request");
	}
}