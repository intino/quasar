package io.intino.ime.box.commands.workspace;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.model.Workspace;
import io.intino.ime.box.workspaces.WorkspaceContainer;

public class RemoveWorkspaceFileCommand extends Command<Boolean> {
	public Workspace workspace;
	public WorkspaceContainer.File file;

	public RemoveWorkspaceFileCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		box.workspaceManager().remove(workspace, file);
		return true;
	}

}
