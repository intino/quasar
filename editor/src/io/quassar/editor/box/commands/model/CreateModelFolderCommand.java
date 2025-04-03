package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.models.File;
import io.quassar.editor.model.Model;

public class CreateModelFolderCommand extends Command<File> {
	public Model model;
	public String name;
	public File parent;

	public CreateModelFolderCommand(EditorBox box) {
		super(box);
	}

	@Override
	public File execute() {
		return box.modelManager().createFolder(model, name, parent);
	}

}
