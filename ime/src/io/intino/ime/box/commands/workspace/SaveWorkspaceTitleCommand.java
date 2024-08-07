package io.intino.ime.box.commands.workspace;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.model.Workspace;

public class SaveWorkspaceTitleCommand extends Command<Boolean> {
	public Workspace workspace;
	public String title;

	public SaveWorkspaceTitleCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		if (title.equals(workspace.title())) return true;
		box.workspaceManager().saveTitle(workspace, title);
		return true;
	}

}
