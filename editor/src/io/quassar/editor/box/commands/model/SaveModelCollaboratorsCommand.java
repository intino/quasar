package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Model;
import io.quassar.editor.model.User;

import java.util.List;

public class SaveModelCollaboratorsCommand extends Command<Boolean> {
	public Model model;
	public List<User> collaborators;

	public SaveModelCollaboratorsCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		model.collaborators(collaborators.stream().map(User::name).toList());
		return true;
	}

}
