package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Model;

public class MakeModelPrivateCommand extends Command<Boolean> {
	public Model model;

	public MakeModelPrivateCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		model.isPrivate(true);
		return true;
	}

}
