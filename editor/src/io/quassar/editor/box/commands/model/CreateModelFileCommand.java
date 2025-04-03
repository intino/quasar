package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.models.File;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.model.Model;

import java.io.InputStream;

public class CreateModelFileCommand extends Command<File> {
	public Model model;
	public String name;
	public InputStream content;
	public File parent;

	public CreateModelFileCommand(EditorBox box) {
		super(box);
	}

	@Override
	public File execute() {
		return box.modelManager().createFile(model, ModelHelper.validWorkspaceFileName(name), content, parent);
	}

}
