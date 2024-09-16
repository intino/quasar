package io.intino.ime.box.commands.model;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.model.Model;

public class CreateModelVersionCommand extends Command<Model> {
	public Model model;
	public Model.Version version;

	public CreateModelVersionCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Model execute() {
		return box.modelManager().createVersion(model, version);
	}
}
