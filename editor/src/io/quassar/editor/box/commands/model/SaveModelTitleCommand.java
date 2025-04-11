package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Model;

public class SaveModelTitleCommand extends Command<Boolean> {
	public Model model;
	public String title;

	public SaveModelTitleCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		model.title(title);
		return true;
	}

}
