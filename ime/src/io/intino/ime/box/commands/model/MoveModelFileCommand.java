package io.intino.ime.box.commands.model;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.box.models.ModelContainer;
import io.intino.ime.model.Model;

public class MoveModelFileCommand extends Command<ModelContainer.File> {
	public Model model;
	public ModelContainer.File file;
	public ModelContainer.File directory;

	public MoveModelFileCommand(ImeBox box) {
		super(box);
	}

	@Override
	public ModelContainer.File execute() {
		return box.modelManager().move(model, file, directory);
	}

}
