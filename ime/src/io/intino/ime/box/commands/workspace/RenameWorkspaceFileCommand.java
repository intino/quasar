package io.intino.ime.box.commands.workspace;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.box.workspaces.WorkspaceContainer;
import io.intino.ime.model.Workspace;

public class RenameWorkspaceFileCommand extends Command<WorkspaceContainer.File> {
	public Workspace workspace;
	public WorkspaceContainer.File file;
	public String newName;

	public RenameWorkspaceFileCommand(ImeBox box) {
		super(box);
	}

	@Override
	public WorkspaceContainer.File execute() {
		return box.workspaceManager().rename(workspace, file, newName);
	}

}
