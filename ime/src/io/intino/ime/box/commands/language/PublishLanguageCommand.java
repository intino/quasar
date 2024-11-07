package io.intino.ime.box.commands.language;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.box.commands.model.ExecuteModelOperationCommand;
import io.intino.ime.model.LanguageLevel;
import io.intino.ime.model.Model;
import io.intino.ime.model.Operation;

public class PublishLanguageCommand extends Command<Command.ExecutionResult> {
	public Model model;
	public LanguageLevel level;
	public String version;

	public PublishLanguageCommand(ImeBox box) {
		super(box);
	}

	@Override
	public ExecutionResult execute() {
		var command = new ExecuteModelOperationCommand(box);
		command.model = model;
		command.operation = new Operation("Build");
		ExecutionResult result = command.execute();
		if (result.success()) box.languageManager().createRelease(model, level, version);
		return result;
	}

}
