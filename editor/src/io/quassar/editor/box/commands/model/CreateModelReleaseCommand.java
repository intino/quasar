package io.quassar.editor.box.commands.model;

import io.intino.alexandria.logger.Logger;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.commands.Command.ExecutionResult;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class CreateModelReleaseCommand extends Command<ExecutionResult> {
	public Model model;
	public String version;

	public CreateModelReleaseCommand(EditorBox box) {
		super(box);
	}

	@Override
	public ExecutionResult execute() {
		ExecutionResult result = compile();
		if (!result.success()) return result;
		saveAccessor(result);
		return resultOf(box.modelManager().createRelease(model, version));
	}

	private ExecutionResult compile() {
		BuildModelCommand command = new BuildModelCommand(box);
		command.author = author;
		command.model = model;
		return command.execute();
	}

	private void saveAccessor(ExecutionResult result) {
		try {
			File destiny = box.archetype().languages().releaseAccessor(Language.nameOf(model.language()), model.name(), version);
			FileUtils.copyInputStreamToFile(result.output(), destiny);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

}
