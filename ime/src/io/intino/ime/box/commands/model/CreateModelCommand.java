package io.intino.ime.box.commands.model;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;
import io.intino.ime.model.Release;

public class CreateModelCommand extends Command<Model> {
	public String id;
	public String label;
	public Release release;
	public String owner;

	public CreateModelCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Model execute() {
		Model model = create();
		updateLanguage(model);
		return model;
	}

	private Model create() {
		return box.modelManager().create(id, label, release, owner, release.language(), author != null);
	}

	private void updateLanguage(Model model) {
		Language language = box.languageManager().get(model.modelingLanguage());
		language.modelsCount(language.modelsCount()+1);
		box.languageManager().save(language);
	}

}
