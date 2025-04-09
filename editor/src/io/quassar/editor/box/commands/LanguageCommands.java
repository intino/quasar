package io.quassar.editor.box.commands;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.language.*;
import io.quassar.editor.model.GavCoordinates;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;

import java.io.File;
import java.util.List;

public class LanguageCommands extends Commands {

	public LanguageCommands(EditorBox box) {
		super(box);
	}

	public Language create(String name, String version, GavCoordinates parent, Language.Level level, String hint, String description, String username) {
		CreateLanguageCommand command = setup(new CreateLanguageCommand(box), username);
		command.name = name;
		command.version = version;
		command.parent = parent;
		command.level = level;
		command.hint = hint;
		command.description = description;
		return command.execute();
	}

	public Language createRelease(Language language, String version, Language.Level level, String username) {
		CreateLanguageReleaseCommand command = setup(new CreateLanguageReleaseCommand(box), username);
		command.language = language;
		command.version = version;
		command.level = level;
		return command.execute();
	}

	public void saveProperties(Language language, String title, String description, String username) {
		SaveLanguagePropertiesCommand command = setup(new SaveLanguagePropertiesCommand(box), username);
		command.language = language;
		command.title = title;
		command.description = description;
		command.execute();
	}

	public void save(Language language, String title, String description, Language.Level level, List<String> tags, File logo, String username) {
		SaveLanguageCommand command = setup(new SaveLanguageCommand(box), username);
		command.language = language;
		command.title = title;
		command.description = description;
		command.level = level;
		command.tags = tags;
		command.logo = logo;
		command.execute();
	}

	public void saveHelp(Language language, LanguageRelease release, String content, String username) {
		SaveLanguageHelpCommand command = setup(new SaveLanguageHelpCommand(box), username);
		command.language = language;
		command.release = release;
		command.content = content;
		command.execute();
	}

	public Boolean remove(Language language, String username) {
		RemoveLanguageCommand command = setup(new RemoveLanguageCommand(box), username);
		command.language = language;
		return command.execute();
	}

	public void saveAccess(Language language, List<String> access, String username) {
		SaveLanguageAccessCommand command = setup(new SaveLanguageAccessCommand(box), username);
		command.language = language;
		command.access = access;
		command.execute();
	}

}
