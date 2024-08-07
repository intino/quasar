package io.intino.ime.box.commands.workspace;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.model.Workspace;

public class MakeWorkspacePublicCommand extends Command<Boolean> {
	public Workspace workspace;

	public MakeWorkspacePublicCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		box.workspaceManager().makePublic(workspace);
		return true;
	}

}
