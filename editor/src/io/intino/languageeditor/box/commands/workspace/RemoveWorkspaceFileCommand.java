package io.intino.languageeditor.box.commands.workspace;

import io.intino.languageeditor.box.LanguageEditorBox;
import io.intino.languageeditor.box.commands.Command;
import io.intino.languageeditor.box.workspaces.Workspace;
import io.intino.languageeditor.box.workspaces.WorkspaceContainer;

public class RemoveWorkspaceFileCommand extends Command<Boolean> {
	public Workspace workspace;
	public WorkspaceContainer.File file;

	public RemoveWorkspaceFileCommand(LanguageEditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		box.workspaceManager().remove(workspace, file);
		return true;
	}

}
