package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Model;

public class CloneModelCommand extends Command<Model> {
	public Model model;
	public String id;
	public String owner;

	public CloneModelCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Model execute() {
		return box.modelManager().clone(model, id, owner);
	}

}
