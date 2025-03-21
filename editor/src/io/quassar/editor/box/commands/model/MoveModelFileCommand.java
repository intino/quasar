package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.models.ModelContainer;
import io.quassar.editor.model.Model;

public class MoveModelFileCommand extends Command<ModelContainer.File> {
	public Model model;
	public ModelContainer.File file;
	public ModelContainer.File directory;

	public MoveModelFileCommand(EditorBox box) {
		super(box);
	}

	@Override
	public ModelContainer.File execute() {
		return box.modelManager().move(model, file, directory);
	}

}
