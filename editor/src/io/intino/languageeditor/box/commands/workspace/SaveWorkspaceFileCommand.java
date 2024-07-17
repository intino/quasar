package io.intino.languageeditor.box.commands.workspace;

import io.intino.languageeditor.box.LanguageEditorBox;
import io.intino.languageeditor.box.commands.Command;
import io.intino.languageeditor.box.workspaces.Workspace;
import io.intino.languageeditor.box.workspaces.WorkspaceContainer;

public class SaveWorkspaceFileCommand extends Command<Boolean> {
	public Workspace workspace;
	public WorkspaceContainer.File file;
	public String content;

	public SaveWorkspaceFileCommand(LanguageEditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		box.workspaceManager().save(workspace, file, content);
		return true;
	}

}
