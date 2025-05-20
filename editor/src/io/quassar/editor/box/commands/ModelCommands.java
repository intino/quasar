package io.quassar.editor.box.commands;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.model.*;
import io.quassar.editor.box.models.File;
import io.quassar.editor.box.ui.types.ModelView;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.model.*;

import java.io.InputStream;
import java.util.List;

public class ModelCommands extends Commands {

	public ModelCommands(EditorBox box) {
		super(box);
	}

	public Model create(String name, String title, String description, GavCoordinates language, String owner, String username) {
		CreateModelCommand command = setup(new CreateModelCommand(box), username);
		command.name = name;
		command.title = title;
		command.description = description;
		command.language = language;
		command.owner = owner;
		return command.execute();
	}

	public Model createTemplate(Language language, LanguageRelease release, String username) {
		CreateModelCommand command = setup(new CreateModelCommand(box), username);
		command.language = GavCoordinates.fromString(language, release);
		command.name = ModelHelper.proposeName();
		command.title = language.name();
		command.description = "";
		command.usage = Model.Usage.Template;
		return command.execute();
	}

	public Model createExample(Language language, LanguageRelease release, String username) {
		CreateExampleModelCommand command = setup(new CreateExampleModelCommand(box), username);
		command.language = language;
		command.release = release;
		return command.execute();
	}

	public Model clone(Model model, String release, String name, String username) {
		CloneModelCommand command = setup(new CloneModelCommand(box), username);
		command.model = model;
		command.release = release;
		command.name = name;
		return command.execute();
	}

	public Boolean addZip(Model model, ModelView view, InputStream content, File parent, String username) {
		AddModelZipCommand command = setup(new AddModelZipCommand(box), username);
		command.model = model;
		command.view = view;
		command.content = content;
		command.parent = parent;
		return command.execute();
	}

	public File createFile(Model model, String name, InputStream content, File parent, String username) {
		CreateModelFileCommand command = setup(new CreateModelFileCommand(box), username);
		command.model = model;
		command.name = name;
		command.content = content;
		command.parent = parent;
		return command.execute();
	}

	public File createFolder(Model model, String name, File parent, String username) {
		CreateModelFolderCommand command = setup(new CreateModelFolderCommand(box), username);
		command.model = model;
		command.name = name;
		command.parent = parent;
		return command.execute();
	}

	public File copy(Model model, String filename, File source, String username) {
		CopyModelFileCommand command = setup(new CopyModelFileCommand(box), username);
		command.model = model;
		command.filename = filename;
		command.source = source;
		return command.execute();
	}

	public void saveProperties(Model model, String title, String description, String username) {
		SaveModelPropertiesCommand command = setup(new SaveModelPropertiesCommand(box), username);
		command.model = model;
		command.title = title;
		command.description = description;
		command.execute();
	}

	public void saveSimpleTitle(Model model, String title, String username) {
		SaveModelSimpleTitleCommand command = setup(new SaveModelSimpleTitleCommand(box), username);
		command.model = model;
		command.title = title;
		command.execute();
	}

	public void saveQualifiedTitle(Model model, String project, String module, String username) {
		SaveModelQualifiedTitleCommand command = setup(new SaveModelQualifiedTitleCommand(box), username);
		command.model = model;
		command.project = project;
		command.module = module;
		command.execute();
	}

	public void save(Model model, List<User> collaborators, String username) {
		SaveModelCollaboratorsCommand command = setup(new SaveModelCollaboratorsCommand(box), username);
		command.model = model;
		command.collaborators = collaborators;
		command.execute();
	}

	public void save(Model model, File file, InputStream content, String username) {
		SaveModelFileCommand command = setup(new SaveModelFileCommand(box), username);
		command.model = model;
		command.file = file;
		command.content = content;
		command.execute();
	}

	public File rename(Model model, String newName, File file, String username) {
		RenameModelFileCommand command = setup(new RenameModelFileCommand(box), username);
		command.model = model;
		command.file = file;
		command.newName = newName;
		return command.execute();
	}

	public File move(Model model, File file, File directory, String username) {
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

	public void updateLanguageVersion(Model model, String version, String username) {
		UpdateModelLanguageVersionCommand command = setup(new UpdateModelLanguageVersionCommand(box), username);
		command.model = model;
		command.version = version;
		command.execute();
	}

	public void remove(Model model, String username) {
		RemoveModelCommand command = setup(new RemoveModelCommand(box), username);
		command.model = model;
		command.execute();
	}

	public void remove(Model model, File file, String username) {
		RemoveModelFileCommand command = setup(new RemoveModelFileCommand(box), username);
		command.model = model;
		command.file = file;
		command.execute();
	}

	public Command.ExecutionResult check(Model model, String username) {
		CheckModelCommand command = setup(new CheckModelCommand(box), username);
		command.model = model;
		command.release = Model.DraftRelease;
		return command.execute();
	}

	public Command.ExecutionResult createRelease(Model model, String version, String username) {
		CreateModelReleaseCommand command = setup(new CreateModelReleaseCommand(box), username);
		command.model = model;
		command.version = version;
		return command.execute();
	}
}
