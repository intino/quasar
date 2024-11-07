package io.intino.ime.box.commands.language;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;

public class RemoveLanguageCommand extends Command<Boolean> {
	public Language language;

	public RemoveLanguageCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		box.languageManager().remove(language);
		Model model = box.modelManager().modelWith(language);
		box.modelManager().remove(model);
		box.serverManager().remove(model);
		return true;
	}

}
