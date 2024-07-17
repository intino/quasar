package io.intino.languageeditor.box.commands.workspace;

import io.intino.languageeditor.box.LanguageEditorBox;
import io.intino.languageeditor.box.commands.Command;
import io.intino.languageeditor.box.workspaces.Workspace;
import io.intino.languageeditor.box.workspaces.WorkspaceContainer;

public class CreateWorkspaceFileCommand extends Command<WorkspaceContainer.File> {
	public Workspace workspace;
	public String filename;
	public WorkspaceContainer.File parent;

	public CreateWorkspaceFileCommand(LanguageEditorBox box) {
		super(box);
	}

	@Override
	public WorkspaceContainer.File execute() {
		return box.workspaceManager().create(workspace, filename, parent);
	}

}
