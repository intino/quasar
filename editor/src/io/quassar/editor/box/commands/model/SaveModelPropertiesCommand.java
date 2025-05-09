package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Model;

public class SaveModelPropertiesCommand extends Command<Boolean> {
	public Model model;
	public String title;
	public String description;

	public SaveModelPropertiesCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		model.title(title.toUpperCase());
		model.description(description);
		return true;
	}

}
