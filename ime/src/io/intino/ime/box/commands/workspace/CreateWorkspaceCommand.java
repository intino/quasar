package io.intino.ime.box.commands.workspace;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.box.workspaces.Workspace;
import io.intino.ime.box.workspaces.WorkspaceContainer;

import java.time.Instant;

public class CreateWorkspaceCommand extends Command<Workspace> {
	public String name;
	public String title;
	public String dsl;

	public CreateWorkspaceCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Workspace execute() {
		Workspace workspace = new Workspace();
		workspace.name(name);
		workspace.title(title);
		workspace.dsl(dsl);
		workspace.owner(new Workspace.User(author(), author()));
		workspace.lastModifyDate(Instant.now());
		return box.workspaceManager().create(workspace);
	}

}
