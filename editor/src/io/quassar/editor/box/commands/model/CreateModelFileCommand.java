package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.models.ModelContainer;
import io.quassar.editor.model.Model;

public class CreateModelFileCommand extends Command<ModelContainer.File> {
	public Model model;
	public String name;
	public String content;
	public ModelContainer.File parent;

	public CreateModelFileCommand(EditorBox box) {
		super(box);
	}

	@Override
	public ModelContainer.File execute() {
		return box.modelManager().createFile(model, name, content, parent);
	}

}
