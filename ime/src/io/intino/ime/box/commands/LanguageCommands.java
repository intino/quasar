package io.intino.ime.box.commands;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.language.CreateLanguageCommand;
import io.intino.ime.box.commands.language.PublishLanguageCommand;
import io.intino.ime.box.commands.language.RemoveLanguageCommand;
import io.intino.ime.box.commands.language.UnPublishLanguageCommand;
import io.intino.ime.model.Language;
import io.intino.ime.model.Workspace;

public class LanguageCommands extends Commands {

	public LanguageCommands(ImeBox box) {
		super(box);
	}

	public Language create(Workspace workspace, String version, String username) {
		CreateLanguageCommand command = setup(new CreateLanguageCommand(box), username);
		command.workspace = workspace;
		command.version = version;
		return command.execute();
	}

	public Language publish(Language language, String username) {
		PublishLanguageCommand command = setup(new PublishLanguageCommand(box), username);
		command.language = language;
		return command.execute();
	}

	public Language unPublish(Language language, String username) {
		UnPublishLanguageCommand command = setup(new UnPublishLanguageCommand(box), username);
		command.language = language;
		return command.execute();
	}

	public Boolean remove(Language language, String username) {
		RemoveLanguageCommand command = setup(new RemoveLanguageCommand(box), username);
		command.language = language;
		return command.execute();
	}

}
