package io.quassar.editor.box.commands;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.model.*;
import io.quassar.editor.box.models.ModelContainer;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;
import io.quassar.editor.model.User;

import java.util.List;
import java.util.Map;

public class ModelCommands extends Commands {

	public ModelCommands(EditorBox box) {
		super(box);
	}

	public Model create(String name, String title, String description, Language language, String owner, String username) {
		CreateModelCommand command = setup(new CreateModelCommand(box), username);
		command.name = name;
		command.title = title;
		command.description = description;
		command.language = language;
		command.owner = owner;
		return command.execute();
	}

	public Model clone(Model model, String name, String owner, String username) {
		CloneModelCommand command = setup(new CloneModelCommand(box), username);
		command.model = model;
		command.id = name;
		command.owner = owner;
		return command.execute();
	}

	public ModelContainer.File createFile(Model model, String name, String content, ModelContainer.File parent, String username) {
		CreateModelFileCommand command = setup(new CreateModelFileCommand(box), username);
		command.model = model;
		command.name = name;
		command.content = content;
		command.parent = parent;
		return command.execute();
	}

	public ModelContainer.File createFolder(Model model, String name, ModelContainer.File parent, String username) {
		CreateModelFolderCommand command = setup(new CreateModelFolderCommand(box), username);
		command.model = model;
		command.name = name;
		command.parent = parent;
		return command.execute();
	}

	public ModelContainer.File copy(Model model, String filename, ModelContainer.File source, String username) {
		CopyModelFileCommand command = setup(new CopyModelFileCommand(box), username);
		command.model = model;
		command.filename = filename;
		command.source = source;
		return command.execute();
	}

	public void save(Model model, String name, String title, String description, String username) {
		SaveModelCommand command = setup(new SaveModelCommand(box), username);
		command.model = model;
		command.name = name;
		command.title = title;
		command.description = description;
		command.execute();
	}

	public void save(Model model, String title, String username) {
		SaveModelTitleCommand command = setup(new SaveModelTitleCommand(box), username);
		command.model = model;
		command.title = title;
		command.execute();
	}

	public void save(Model model, List<User> collaborators, String username) {
		SaveModelCollaboratorsCommand command = setup(new SaveModelCollaboratorsCommand(box), username);
		command.model = model;
		command.collaborators = collaborators;
		command.execute();
	}

	public void save(Model model, Map<String, String> tokens, String username) {
		SaveModelTokensCommand command = setup(new SaveModelTokensCommand(box), username);
		command.model = model;
		command.tokens = tokens;
		command.execute();
	}

	public void save(Model model, ModelContainer.File file, String content, String username) {
		SaveModelFileCommand command = setup(new SaveModelFileCommand(box), username);
		command.model = model;
		command.file = file;
		command.content = content;
		command.execute();
	}

	public ModelContainer.File rename(Model model, String newName, ModelContainer.File file, String username) {
		RenameModelFileCommand command = setup(new RenameModelFileCommand(box), username);
		command.model = model;
		command.file = file;
		command.newName = newName;
		return command.execute();
	}

	public ModelContainer.File move(Model model, ModelContainer.File file, ModelContainer.File directory, String username) {
		MoveModelFileCommand command = setup(new MoveModelFileCommand(box), username);
		command.model = model;
		command.file = file;
		command.directory = directory;
		return command.execute();
	}

	public void makePrivate(Model model, String username) {
		MakeModelPrivateCommand command = setup(new MakeModelPrivateCommand(box), username);
		command.model = model;
		command.execute();
	}

	public void makePublic(Model model, String username) {
		MakeModelPublicCommand command = setup(new MakeModelPublicCommand(box), username);
		command.model = model;
		command.execute();
	}

	public void remove(Model model, String username) {
		RemoveModelCommand command = setup(new RemoveModelCommand(box), username);
		command.model = model;
		command.execute();
	}

	public void remove(Model model, ModelContainer.File file, String username) {
		RemoveModelFileCommand command = setup(new RemoveModelFileCommand(box), username);
		command.model = model;
		command.file = file;
		command.execute();
	}

	public Command.ExecutionResult build(Model model, String username) {
		BuildModelCommand command = setup(new BuildModelCommand(box), username);
		command.model = model;
		return command.execute();
	}

	public Command.ExecutionResult createRelease(Model model, String version, String username) {
		CreateModelReleaseCommand command = setup(new CreateModelReleaseCommand(box), username);
		command.model = model;
		command.version = version;
		return command.execute();
	}
}
