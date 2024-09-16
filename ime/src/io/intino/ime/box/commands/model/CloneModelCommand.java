package io.intino.ime.box.commands.model;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;
import io.intino.ime.model.User;

public class CloneModelCommand extends Command<Model> {
	public Model model;
	public String version;
	public String name;
	public String title;
	public User owner;

	public CloneModelCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Model execute() {
		Language language = box.languageManager().get(model.language());
		return box.modelManager().clone(model, language, name, title, owner);
	}

}
