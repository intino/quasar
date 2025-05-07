package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.commands.ModelCommands;
import io.quassar.editor.model.GavCoordinates;
import io.quassar.editor.model.Model;

public class UpdateModelLanguageVersionCommand extends Command<Boolean> {
	public Model model;
	public String version;

	public UpdateModelLanguageVersionCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		box.modelManager().updateLanguageVersion(model, version);
		return true;
	}

}
