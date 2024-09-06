package io.intino.ime.box.commands.model;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.model.User;
import io.intino.ime.model.Model;

import java.time.Instant;

public class CreateModelCommand extends Command<Model> {
	public String name;
	public String title;
	public String dsl;
	public User owner;

	public CreateModelCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Model execute() {
		Model model = new Model();
		model.name(name);
		model.title(title);
		model.language(dsl);
		model.owner(owner);
		model.lastModifyDate(Instant.now());
		return box.modelManager().create(model);
	}

}
