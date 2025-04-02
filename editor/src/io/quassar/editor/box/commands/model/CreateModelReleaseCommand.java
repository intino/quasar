package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.commands.Command.ExecutionResult;
import io.quassar.editor.model.Model;

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
		return resultOf(box.modelManager().createRelease(model, version, result.output()));
	}

	private ExecutionResult compile() {
		BuildModelCommand command = new BuildModelCommand(box);
		command.author = author;
		command.model = model;
		command.release = version;
		return command.execute();
	}

}
