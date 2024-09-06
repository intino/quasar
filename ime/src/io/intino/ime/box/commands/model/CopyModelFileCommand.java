package io.intino.ime.box.commands.model;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.box.models.ModelContainer;
import io.intino.ime.model.Model;

public class CopyModelFileCommand extends Command<ModelContainer.File> {
	public Model model;
	public String filename;
	public ModelContainer.File source;

	public CopyModelFileCommand(ImeBox box) {
		super(box);
	}

	@Override
	public ModelContainer.File execute() {
		return box.modelManager().copy(model, filename, source);
	}

}
