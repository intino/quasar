package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.models.File;
import io.quassar.editor.model.Model;

import java.io.InputStream;

public class SaveModelFileCommand extends Command<Boolean> {
	public Model model;
	public File file;
	public InputStream content;

	public SaveModelFileCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		box.modelManager().save(model, file, content);
		return true;
	}

}
