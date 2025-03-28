package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Model;

import java.util.Map;

public class SaveModelTokensCommand extends Command<Boolean> {
	public Model model;
	public Map<String, String> tokens;

	public SaveModelTokensCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		model.tokens(tokens);
		box.modelManager().save(model);
		return true;
	}

}
