package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.models.File;
import io.quassar.editor.model.Model;

public class MoveModelFileCommand extends Command<File> {
	public Model model;
	public File file;
	public File directory;

	public MoveModelFileCommand(EditorBox box) {
		super(box);
	}

	@Override
	public File execute() {
		return box.modelManager().move(model, file, directory);
	}

}
