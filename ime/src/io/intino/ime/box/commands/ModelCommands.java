package io.intino.ime.box.commands;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command.ExecutionResult;
import io.intino.ime.box.commands.model.*;
import io.intino.ime.box.models.ModelContainer;
import io.intino.ime.box.scaffolds.ScaffoldFactory;
import io.intino.ime.model.Model;
import io.intino.ime.model.Operation;
import io.intino.ime.model.Release;

public class ModelCommands extends Commands {

	public ModelCommands(ImeBox box) {
		super(box);
	}

	public Model create(String id, String label, Release release, ScaffoldFactory.Language programmingLanguage, ScaffoldFactory.Scaffold scaffold, String owner, String username) {
		CreateModelCommand command = setup(new CreateModelCommand(box), username);
		command.id = id;
		command.label = label;
		command.release = release;
		command.programmingLanguage = programmingLanguage;
		command.scaffold = scaffold;
		command.owner = owner;
		return command.execute();
	}

	public Model clone(Model model, String name, String title, String owner, String username) {
		CloneModelCommand command = setup(new CloneModelCommand(box), username);
		command.model = model;
		command.id = name;
		command.label = title;
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

	public void save(Model model, ModelContainer.File file, String content, String username) {
		SaveModelFileCommand command = setup(new SaveModelFileCommand(box), username);
		command.model = model;
		command.file = file;
		command.content = content;
		command.execute();
	}

	public void saveLabel(Model model, String label, String username) {
		SaveModelLabelCommand command = setup(new SaveModelLabelCommand(box), username);
		command.model = model;
		command.label = label;
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

	public ExecutionResult execute(Model model, Operation operation, String username) {
		ExecuteModelOperationCommand command = setup(new ExecuteModelOperationCommand(box), username);
		command.model = model;
		command.operation = operation;
		return command.execute();
	}

	public void saveGitSettings(Model model, String url, String branch, String username) {
		SaveModelGitSettingsCommand command = setup(new SaveModelGitSettingsCommand(box), username);
		command.model = model;
		command.url = url;
		command.branch = branch;
		command.execute();
	}
}
