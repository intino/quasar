package io.intino.ime.box.commands.model;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.box.commands.language.RemoveLanguageCommand;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;

public class RemoveModelCommand extends Command<Boolean> {
	public Model model;

	public RemoveModelCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		Language language = box.languageManager().allLanguages().stream().filter(this::matches).findFirst().orElse(null);
		if (language != null && ModelHelper.isMetamodel(model, box)) removeLanguage(language);
		else removeModel();
		return true;
	}

	private boolean matches(Language language) {
		Model languageModel = box.modelManager().modelWith(language);
		return languageModel != null && languageModel.id().equals(model.id());
	}

	private void removeLanguage(Language language) {
		RemoveLanguageCommand command = new RemoveLanguageCommand(box);
		command.author = author;
		command.language = language;
		command.execute();
	}

	private void removeModel() {
		box.modelManager().remove(model);
		updateLanguage(model);
	}

	private void updateLanguage(Model model) {
		Language language = box.languageManager().get(model.modelingLanguage());
		language.modelsCount(language.modelsCount()-1);
		box.languageManager().save(language);
	}

}
