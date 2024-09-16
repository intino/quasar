package io.intino.ime.box.commands;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.language.*;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;

public class LanguageCommands extends Commands {

	public LanguageCommands(ImeBox box) {
		super(box);
	}

	public Language create(Model model, Model.Version version, String username) {
		CreateLanguageCommand command = setup(new CreateLanguageCommand(box), username);
		command.model = model;
		command.version = version;
		return command.execute();
	}

	public void save(Language language, String builderUrl, String username) {
		SaveLanguageCommand command = setup(new SaveLanguageCommand(box), username);
		command.language = language;
		command.builderUrl = builderUrl;
		command.execute();
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
