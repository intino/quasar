package io.quassar.editor.box.commands;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.language.CreateLanguageCommand;
import io.quassar.editor.box.commands.language.RemoveLanguageCommand;
import io.quassar.editor.box.commands.language.SaveLanguageCommand;
import io.quassar.editor.model.Language;

import java.io.File;
import java.util.List;

public class LanguageCommands extends Commands {

	public LanguageCommands(EditorBox box) {
		super(box);
	}

	public Language create(String name, String parent, Language.Level level, String description, File logo, String username) {
		CreateLanguageCommand command = setup(new CreateLanguageCommand(box), username);
		command.name = name;
		command.parent = parent;
		command.level = level;
		command.description = description;
		command.logo = logo;
		return command.execute();
	}

	public void save(Language language, String description, Language.Level level, List<String> tags, File logo, String username) {
		SaveLanguageCommand command = setup(new SaveLanguageCommand(box), username);
		command.language = language;
		command.description = description;
		command.level = level;
		command.tags = tags;
		command.logo = logo;
		command.execute();
	}

	public Boolean remove(Language language, String username) {
		RemoveLanguageCommand command = setup(new RemoveLanguageCommand(box), username);
		command.language = language;
		return command.execute();
	}

}
