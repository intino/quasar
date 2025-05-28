package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Model;

public class SaveModelDescriptionCommand extends Command<Boolean> {
	public Model model;
	public String description;

	public SaveModelDescriptionCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		model.description(description);
		return true;
	}

}
