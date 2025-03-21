package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.models.ModelContainer;
import io.quassar.editor.model.Model;

public class CreateModelFolderCommand extends Command<ModelContainer.File> {
	public Model model;
	public String name;
	public ModelContainer.File parent;

	public CreateModelFolderCommand(EditorBox box) {
		super(box);
	}

	@Override
	public ModelContainer.File execute() {
		return box.modelManager().createFolder(model, name, parent);
	}

}
