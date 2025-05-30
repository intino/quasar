package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Model;

public class SaveModelSimpleTitleCommand extends Command<Boolean> {
	public Model model;
	public String title;

	public SaveModelSimpleTitleCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		model.title(title.toUpperCase());
		return true;
	}

}
