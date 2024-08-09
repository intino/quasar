package io.intino.ime.box.commands.workspace;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.box.workspaces.WorkspaceContainer;
import io.intino.ime.model.Workspace;

public class CreateWorkspaceFolderCommand extends Command<WorkspaceContainer.File> {
	public Workspace workspace;
	public String name;
	public WorkspaceContainer.File parent;

	public CreateWorkspaceFolderCommand(ImeBox box) {
		super(box);
	}

	@Override
	public WorkspaceContainer.File execute() {
		return box.workspaceManager().createFolder(workspace, name, parent);
	}

}
