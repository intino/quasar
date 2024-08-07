package io.intino.ime.box.commands.workspace;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.model.User;
import io.intino.ime.model.Workspace;

import java.time.Instant;

public class CloneWorkspaceCommand extends Command<Workspace> {
	public Workspace workspace;
	public String name;
	public String title;

	public CloneWorkspaceCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Workspace execute() {
		return box.workspaceManager().clone(workspace, name, title);
	}

}
