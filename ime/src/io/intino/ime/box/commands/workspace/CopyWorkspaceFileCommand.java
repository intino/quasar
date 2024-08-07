package io.intino.ime.box.commands.workspace;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.box.workspaces.WorkspaceContainer;
import io.intino.ime.model.Workspace;

public class CopyWorkspaceFileCommand extends Command<WorkspaceContainer.File> {
	public Workspace workspace;
	public String filename;
	public WorkspaceContainer.File source;

	public CopyWorkspaceFileCommand(ImeBox box) {
		super(box);
	}

	@Override
	public WorkspaceContainer.File execute() {
		return box.workspaceManager().copy(workspace, filename, source);
	}

}
