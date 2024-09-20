package io.intino.ime.box.commands.model;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;

public class RemoveModelCommand extends Command<Boolean> {
	public Model model;

	public RemoveModelCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		box.modelManager().remove(model);
		updateLanguage(model);
		return true;
	}

	private void updateLanguage(Model model) {
		Language language = box.languageManager().get(model.modelingLanguage());
		language.modelsCount(language.modelsCount()-1);
		box.languageManager().save(language);
	}

}
