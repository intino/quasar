package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Model;

public class SaveModelPropertiesCommand extends Command<Boolean> {
	public Model model;
	public String hint;
	public String description;

	public SaveModelPropertiesCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		model.hint(hint);
		model.description(description);
		box.modelManager().save(model);
		return true;
	}

}
