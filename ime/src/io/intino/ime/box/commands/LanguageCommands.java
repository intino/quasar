package io.intino.ime.box.commands;

import io.intino.alexandria.Resource;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command.ExecutionResult;
import io.intino.ime.box.commands.language.*;
import io.intino.ime.box.scaffolds.ScaffoldFactory;
import io.intino.ime.model.*;

import java.util.List;

public class LanguageCommands extends Commands {

	public LanguageCommands(ImeBox box) {
		super(box);
	}

	public Language create(String name, Release parent, String group, ScaffoldFactory.Language programmingLanguage, ScaffoldFactory.Scaffold scaffold, String description, Resource logo, boolean isPrivate, String username) {
		CreateLanguageCommand command = setup(new CreateLanguageCommand(box), username);
		command.name = name;
		command.parent = parent;
		command.group = group;
		command.programmingLanguage = programmingLanguage;
		command.scaffold = scaffold;
		command.description = description;
		command.logo = logo;
		command.isPrivate = isPrivate;
		return command.execute();
	}

	public void save(Language language, String group, String description, boolean isPrivate, String builder, Resource logo, List<Operation> operations, List<String> programmingLanguages, List<String> tags, String username) {
		SaveLanguageCommand command = setup(new SaveLanguageCommand(box), username);
		command.language = language;
		command.group = group;
		command.description = description;
		command.isPrivate = isPrivate;
		command.builder = builder;
		command.logo = logo;
		command.programmingLanguages = programmingLanguages;
		command.operations = operations;
		command.tags = tags;
		command.execute();
	}

	public Boolean remove(Language language, String username) {
		RemoveLanguageCommand command = setup(new RemoveLanguageCommand(box), username);
		command.language = language;
		return command.execute();
	}

	public ExecutionResult publish(Model model, LanguageLevel level, String version, String username) {
		PublishLanguageCommand command = setup(new PublishLanguageCommand(box), username);
		command.model = model;
		command.level = level;
		command.version = version;
		return command.execute();
	}

}
