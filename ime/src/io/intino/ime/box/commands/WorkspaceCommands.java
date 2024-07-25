package io.intino.ime.box.commands;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.workspace.CreateWorkspaceFileCommand;
import io.intino.ime.box.commands.workspace.RemoveWorkspaceFileCommand;
import io.intino.ime.box.commands.workspace.SaveWorkspaceFileCommand;
import io.intino.ime.box.workspaces.Workspace;
import io.intino.ime.box.workspaces.WorkspaceContainer;

public class WorkspaceCommands extends Commands {

	public WorkspaceCommands(ImeBox box) {
		super(box);
	}

	public WorkspaceContainer.File create(Workspace workspace, String filename, WorkspaceContainer.File parent, String username) {
		CreateWorkspaceFileCommand command = setup(new CreateWorkspaceFileCommand(box), username);
		command.workspace = workspace;
		command.filename = filename;
		command.parent = parent;
		return command.execute();
	}

	public void save(Workspace workspace, WorkspaceContainer.File file, String content, String username) {
		SaveWorkspaceFileCommand command = setup(new SaveWorkspaceFileCommand(box), username);
		command.workspace = workspace;
		command.file = file;
		command.content = content;
		command.execute();
	}

	public void remove(Workspace workspace, WorkspaceContainer.File file, String username) {
		RemoveWorkspaceFileCommand command = setup(new RemoveWorkspaceFileCommand(box), username);
		command.workspace = workspace;
		command.file = file;
		command.execute();
	}

}
