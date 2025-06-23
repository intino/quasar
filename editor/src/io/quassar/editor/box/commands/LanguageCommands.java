package io.quassar.editor.box.commands;

import io.intino.alexandria.ui.displays.events.actionable.ToggleEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command.CommandResult;
import io.quassar.editor.box.commands.language.*;
import io.quassar.editor.model.*;

import java.io.File;
import java.util.List;

public class LanguageCommands extends Commands {

	public LanguageCommands(EditorBox box) {
		super(box);
	}

	public Language create(Collection collection, String name, Model metamodel, Language.Level level, boolean isPrivate, File logo, String username) {
		CreateLanguageCommand command = setup(new CreateLanguageCommand(box), username);
		command.collection = collection;
		command.name = name;
		command.metamodel = metamodel;
		command.level = level;
		command.isPrivate = isPrivate;
		command.logo = logo;
		return command.execute();
	}

	public CommandResult createRelease(Language language, String version, String username) {
		CreateLanguageReleaseCommand command = setup(new CreateLanguageReleaseCommand(box), username);
		command.language = language;
		command.version = version;
		return command.execute();
	}

	public void save(Language language, LanguageProperty property, Object value, String username) {
		SaveLanguagePropertyCommand command = setup(new SaveLanguagePropertyCommand(box), username);
		command.language = language;
		command.property = property;
		command.value = value;
		command.execute();
	}

	public Language rename(Language language, String newName, String username) {
		RenameLanguageCommand command = setup(new RenameLanguageCommand(box), username);
		command.language = language;
		command.newId = newName;
		return command.execute();
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

	public void saveHelp(Language language, String release, String content, String username) {
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

	public Boolean remove(Language language, String release, String username) {
		RemoveLanguageReleaseCommand command = setup(new RemoveLanguageReleaseCommand(box), username);
		command.language = language;
		command.release = release;
		return command.execute();
	}

	public LanguageExecution createExecution(Language language, String release, String name, LanguageExecution.Type type, String username) {
		CreateLanguageReleaseExecutionCommand command = setup(new CreateLanguageReleaseExecutionCommand(box), username);
		command.language = language;
		command.release = release;
		command.name = name;
		command.type = type;
		return command.execute();
	}

	public void saveExecution(Language language, String release, String name, LanguageExecution.Type type, String content, String username) {
		SaveLanguageReleaseExecutionCommand command = setup(new SaveLanguageReleaseExecutionCommand(box), username);
		command.language = language;
		command.release = release;
		command.name = name;
		command.type = type;
		command.content = content;
		command.execute();
	}

	public void saveExecutionProperties(Language language, String release, String installationUrl, String username) {
		SaveLanguageReleaseExecutionPropertiesCommand command = setup(new SaveLanguageReleaseExecutionPropertiesCommand(box), username);
		command.language = language;
		command.release = release;
		command.installationUrl = installationUrl;
		command.execute();
	}

	public void makePrivate(Language language, String username) {
		MakeLanguagePrivateCommand command = setup(new MakeLanguagePrivateCommand(box), username);
		command.language = language;
		command.execute();
	}

	public void makePublic(Language language, String username) {
		MakeLanguagePublicCommand command = setup(new MakeLanguagePublicCommand(box), username);
		command.language = language;
		command.execute();
	}

}
