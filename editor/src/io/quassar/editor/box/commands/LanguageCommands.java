package io.quassar.editor.box.commands;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.language.*;
import io.quassar.editor.model.Language;

import java.io.File;
import java.util.List;

public class LanguageCommands extends Commands {

	public LanguageCommands(EditorBox box) {
		super(box);
	}

	public Language create(String name, String version, String parent, Language.Level level, String hint, String description, String username) {
		CreateLanguageCommand command = setup(new CreateLanguageCommand(box), username);
		command.name = name;
		command.version = version;
		command.parent = parent;
		command.level = level;
		command.hint = hint;
		command.description = description;
		return command.execute();
	}

	public Language deploy(String name, String version, Language.Level level, String username) {
		DeployLanguageCommand command = setup(new DeployLanguageCommand(box), username);
		command.name = name;
		command.version = version;
		command.level = level;
		return command.execute();
	}

	public void saveProperties(Language language, String hint, String description, String username) {
		SaveLanguagePropertiesCommand command = setup(new SaveLanguagePropertiesCommand(box), username);
		command.language = language;
		command.hint = hint;
		command.description = description;
		command.execute();
	}

	public void save(Language language, String hint, String description, String fileExtension, Language.Level level, List<String> tags, File logo, String username) {
		SaveLanguageCommand command = setup(new SaveLanguageCommand(box), username);
		command.language = language;
		command.hint = hint;
		command.description = description;
		command.fileExtension = fileExtension;
		command.level = level;
		command.tags = tags;
		command.logo = logo;
		command.execute();
	}

	public void saveReadme(Language language, String content, String username) {
		SaveLanguageReadmeCommand command = setup(new SaveLanguageReadmeCommand(box), username);
		command.language = language;
		command.content = content;
		command.execute();
	}

	public Boolean remove(Language language, String username) {
		RemoveLanguageCommand command = setup(new RemoveLanguageCommand(box), username);
		command.language = language;
		return command.execute();
	}

	public void makePrivate(Language language, List<String> accessPatterns, String username) {
		MakeLanguagePrivateCommand command = setup(new MakeLanguagePrivateCommand(box), username);
		command.language = language;
		command.accessPatterns = accessPatterns;
		command.execute();
	}

	public void makePublic(Language language, String username) {
		MakeLanguagePublicCommand command = setup(new MakeLanguagePublicCommand(box), username);
		command.language = language;
		command.execute();
	}

}
