package io.intino.ime.box.commands;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.workspace.*;
import io.intino.ime.model.User;
import io.intino.ime.model.Workspace;
import io.intino.ime.box.workspaces.WorkspaceContainer;

public class WorkspaceCommands extends Commands {

	public WorkspaceCommands(ImeBox box) {
		super(box);
	}

	public Workspace create(String name, String title, String dsl, User owner, String username) {
		CreateWorkspaceCommand command = setup(new CreateWorkspaceCommand(box), username);
		command.name = name;
		command.title = title;
		command.dsl = dsl;
		command.owner = owner;
		return command.execute();
	}

	public Workspace clone(Workspace workspace, String name, String title, String username) {
		CloneWorkspaceCommand command = setup(new CloneWorkspaceCommand(box), username);
		command.workspace = workspace;
		command.name = name;
		command.title = title;
		return command.execute();
	}

	public WorkspaceContainer.File createFile(Workspace workspace, String name, String content, WorkspaceContainer.File parent, String username) {
		CreateWorkspaceFileCommand command = setup(new CreateWorkspaceFileCommand(box), username);
		command.workspace = workspace;
		command.name = name;
		command.content = content;
		command.parent = parent;
		return command.execute();
	}

	public WorkspaceContainer.File createFolder(Workspace workspace, String name, WorkspaceContainer.File parent, String username) {
		CreateWorkspaceFolderCommand command = setup(new CreateWorkspaceFolderCommand(box), username);
		command.workspace = workspace;
		command.name = name;
		command.parent = parent;
		return command.execute();
	}

	public WorkspaceContainer.File copy(Workspace workspace, String filename, WorkspaceContainer.File source, String username) {
		CopyWorkspaceFileCommand command = setup(new CopyWorkspaceFileCommand(box), username);
		command.workspace = workspace;
		command.filename = filename;
		command.source = source;
		return command.execute();
	}

	public void save(Workspace workspace, WorkspaceContainer.File file, String content, String username) {
		SaveWorkspaceFileCommand command = setup(new SaveWorkspaceFileCommand(box), username);
		command.workspace = workspace;
		command.file = file;
		command.content = content;
		command.execute();
	}

	public WorkspaceContainer.File rename(Workspace workspace, String newName, WorkspaceContainer.File file, String username) {
		RenameWorkspaceFileCommand command = setup(new RenameWorkspaceFileCommand(box), username);
		command.workspace = workspace;
		command.file = file;
		command.newName = newName;
		return command.execute();
	}

	public WorkspaceContainer.File move(Workspace workspace, WorkspaceContainer.File file, WorkspaceContainer.File directory, String username) {
		MoveWorkspaceFileCommand command = setup(new MoveWorkspaceFileCommand(box), username);
		command.workspace = workspace;
		command.file = file;
		command.directory = directory;
		return command.execute();
	}

	public void makePrivate(Workspace workspace, String token, String username) {
		MakeWorkspacePrivateCommand command = setup(new MakeWorkspacePrivateCommand(box), username);
		command.workspace = workspace;
		command.token = token;
		command.execute();
	}

	public void makePublic(Workspace workspace, String username) {
		MakeWorkspacePublicCommand command = setup(new MakeWorkspacePublicCommand(box), username);
		command.workspace = workspace;
		command.execute();
	}

	public void saveTitle(Workspace workspace, String title, String username) {
		SaveWorkspaceTitleCommand command = setup(new SaveWorkspaceTitleCommand(box), username);
		command.workspace = workspace;
		command.title = title;
		command.execute();
	}

	public void remove(Workspace workspace, String username) {
		RemoveWorkspaceCommand command = setup(new RemoveWorkspaceCommand(box), username);
		command.workspace = workspace;
		command.execute();
	}

	public void remove(Workspace workspace, WorkspaceContainer.File file, String username) {
		RemoveWorkspaceFileCommand command = setup(new RemoveWorkspaceFileCommand(box), username);
		command.workspace = workspace;
		command.file = file;
		command.execute();
	}

}
