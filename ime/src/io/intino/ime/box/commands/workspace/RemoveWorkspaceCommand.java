package io.intino.ime.box.commands.workspace;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.box.workspaces.WorkspaceContainer;
import io.intino.ime.model.Workspace;

public class RemoveWorkspaceCommand extends Command<Boolean> {
	public Workspace workspace;

	public RemoveWorkspaceCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		box.workspaceManager().remove(workspace);
		return true;
	}

}
