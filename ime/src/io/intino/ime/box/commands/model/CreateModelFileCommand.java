package io.intino.ime.box.commands.model;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.model.Model;
import io.intino.ime.box.models.ModelContainer;

public class CreateModelFileCommand extends Command<ModelContainer.File> {
	public Model model;
	public String name;
	public String content;
	public ModelContainer.File parent;

	public CreateModelFileCommand(ImeBox box) {
		super(box);
	}

	@Override
	public ModelContainer.File execute() {
		return box.modelManager().createFile(model, name, content, parent);
	}

}
