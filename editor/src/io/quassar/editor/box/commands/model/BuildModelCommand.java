package io.quassar.editor.box.commands.model;

import io.intino.builderservice.schemas.Message;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.builder.ModelBuilder;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.commands.Command.ExecutionResult;
import io.quassar.editor.model.Model;

import java.io.IOException;
import java.util.List;

public class BuildModelCommand extends Command<ExecutionResult> {
	public Model model;

	public BuildModelCommand(EditorBox box) {
		super(box);
	}

	@Override
	public ExecutionResult execute() {
		return compile();
	}

	private ExecutionResult compile() {
		try {
			ModelBuilder builder = new ModelBuilder(model, Model.DraftRelease, box);
			return ExecutionResult.build(builder.build(author()));
		} catch (IOException e) {
			return ExecutionResult.build(List.of(new Message().kind(Message.Kind.ERROR).content(e.getMessage())));
		}
	}

}
