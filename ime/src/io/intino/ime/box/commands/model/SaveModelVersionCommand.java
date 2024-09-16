package io.intino.ime.box.commands.model;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.model.Model;

public class SaveModelVersionCommand extends Command<Model> {
	public Model model;
	public Model.Version version;

	public SaveModelVersionCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Model execute() {
		return box.modelManager().saveVersion(model, version);
	}
}
