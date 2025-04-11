package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Model;

public class MakeModelPublicCommand extends Command<Boolean> {
	public Model model;

	public MakeModelPublicCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		model.isPrivate(false);
		return true;
	}

}
