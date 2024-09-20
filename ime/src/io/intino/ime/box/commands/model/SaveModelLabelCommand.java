package io.intino.ime.box.commands.model;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.model.Model;

public class SaveModelLabelCommand extends Command<Boolean> {
	public Model model;
	public String label;

	public SaveModelLabelCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		if (label.equals(model.label())) return true;
		model.label(label);
		box.modelManager().save(model);
		return true;
	}

}
