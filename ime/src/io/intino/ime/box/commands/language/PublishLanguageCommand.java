package io.intino.ime.box.commands.language;

import io.intino.builderservice.schemas.Message;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.box.commands.model.ExecuteModelOperationCommand;
import io.intino.ime.model.LanguageLevel;
import io.intino.ime.model.Model;
import io.intino.ime.model.Operation;
import io.intino.ime.model.Release;

import java.util.List;

public class PublishLanguageCommand extends Command<Release> {
	public Model model;
	public LanguageLevel level;
	public String version;

	public PublishLanguageCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Release execute() {
		var command = new ExecuteModelOperationCommand(box);
		command.model = model;
		command.operation = new Operation("Build");
		List<Message> execute = command.execute();
		if (execute.stream().anyMatch(m -> m.kind().equals(Message.Kind.ERROR))) return null;
		return box.languageManager().createRelease(model, level, version);
	}

}
