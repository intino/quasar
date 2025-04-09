package io.quassar.editor.box.commands.model;

import io.intino.builderservice.schemas.Message;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.builder.ModelChecker;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.commands.Command.ExecutionResult;
import io.quassar.editor.model.Model;

import java.io.IOException;
import java.util.List;

public class CheckModelCommand extends Command<ExecutionResult> {
	public Model model;
	public String release;

	public CheckModelCommand(EditorBox box) {
		super(box);
	}

	@Override
	public ExecutionResult execute() {
		try {
			ModelChecker checker = new ModelChecker(model, release, box);
			return ExecutionResult.check(checker.check(author()));
		} catch (IOException e) {
			return ExecutionResult.check(List.of(new Message().kind(Message.Kind.ERROR).content(e.getMessage())));
		}
	}

}
