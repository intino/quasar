package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.util.ShortIdGenerator;
import io.quassar.editor.model.Model;

public class CloneModelCommand extends Command<Model> {
	public Model model;
	public String release;
	public String name;

	public CloneModelCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Model execute() {
		return box.modelManager().clone(model, release, ShortIdGenerator.generate(), name, author);
	}

}
