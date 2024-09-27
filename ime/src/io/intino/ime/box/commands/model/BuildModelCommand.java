package io.intino.ime.box.commands.model;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.model.Model;

public class BuildModelCommand extends Command<Boolean> {
	public Model model;

	public BuildModelCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		// TODO OR
		return true;
	}

}
