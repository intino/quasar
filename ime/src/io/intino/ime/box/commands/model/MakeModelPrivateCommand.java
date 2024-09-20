package io.intino.ime.box.commands.model;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.model.Model;

public class MakeModelPrivateCommand extends Command<Boolean> {
	public Model model;

	public MakeModelPrivateCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		box.modelManager().makePrivate(model);
		return true;
	}

}
