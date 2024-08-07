package io.intino.ime.box.commands.workspace;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.model.Workspace;

public class MakeWorkspacePrivateCommand extends Command<Boolean> {
	public Workspace workspace;
	public String token;

	public MakeWorkspacePrivateCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		box.workspaceManager().makePrivate(workspace, token);
		return true;
	}

}
