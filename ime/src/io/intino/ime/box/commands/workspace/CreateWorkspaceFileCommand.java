package io.intino.ime.box.commands.workspace;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.model.Workspace;
import io.intino.ime.box.workspaces.WorkspaceContainer;

public class CreateWorkspaceFileCommand extends Command<WorkspaceContainer.File> {
	public Workspace workspace;
	public String filename;
	public WorkspaceContainer.File parent;

	public CreateWorkspaceFileCommand(ImeBox box) {
		super(box);
	}

	@Override
	public WorkspaceContainer.File execute() {
		return box.workspaceManager().create(workspace, filename, parent);
	}

}
