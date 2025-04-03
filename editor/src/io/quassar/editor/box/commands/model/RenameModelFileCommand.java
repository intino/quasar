package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.models.File;
import io.quassar.editor.model.Model;

public class RenameModelFileCommand extends Command<File> {
	public Model model;
	public File file;
	public String newName;

	public RenameModelFileCommand(EditorBox box) {
		super(box);
	}

	@Override
	public File execute() {
		return box.modelManager().rename(model, file, newName);
	}

}
