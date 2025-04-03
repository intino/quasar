package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.models.File;
import io.quassar.editor.model.Model;

public class RemoveModelFileCommand extends Command<Boolean> {
	public Model model;
	public File file;

	public RemoveModelFileCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		box.modelManager().remove(model, file);
		return true;
	}

}
