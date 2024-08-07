package io.intino.ime.box.commands.workspace;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.model.User;
import io.intino.ime.model.Workspace;

import java.time.Instant;

public class CreateWorkspaceCommand extends Command<Workspace> {
	public String name;
	public String title;
	public String dsl;
	public User owner;

	public CreateWorkspaceCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Workspace execute() {
		Workspace workspace = new Workspace();
		workspace.name(name);
		workspace.title(title);
		workspace.language(dsl);
		workspace.owner(owner);
		workspace.lastModifyDate(Instant.now());
		return box.workspaceManager().create(workspace);
	}

}
