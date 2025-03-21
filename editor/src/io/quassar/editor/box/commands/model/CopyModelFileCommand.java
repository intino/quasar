package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.models.ModelContainer;
import io.quassar.editor.model.Model;

public class CopyModelFileCommand extends Command<ModelContainer.File> {
	public Model model;
	public String filename;
	public ModelContainer.File source;

	public CopyModelFileCommand(EditorBox box) {
		super(box);
	}

	@Override
	public ModelContainer.File execute() {
		return box.modelManager().copy(model, filename, source);
	}

}
