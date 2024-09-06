package io.intino.ime.box.commands.model;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.model.Model;
import io.intino.ime.box.models.ModelContainer;

public class SaveModelFileCommand extends Command<Boolean> {
	public Model model;
	public ModelContainer.File file;
	public String content;

	public SaveModelFileCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		box.modelManager().save(model, file, content);
		return true;
	}

}
