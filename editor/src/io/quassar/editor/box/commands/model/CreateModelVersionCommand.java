package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.util.ZipHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;
import io.quassar.editor.model.OperationResult;

public class CreateModelVersionCommand extends Command<OperationResult> {
	public Model model;
	public String version;

	public CreateModelVersionCommand(EditorBox box) {
		super(box);
	}

	@Override
	public OperationResult execute() {
		return box.modelManager().createVersion(model, version);
	}

}
