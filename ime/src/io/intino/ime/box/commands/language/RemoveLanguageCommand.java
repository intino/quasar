package io.intino.ime.box.commands.language;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.model.Language;

public class RemoveLanguageCommand extends Command<Boolean> {
	public Language language;

	public RemoveLanguageCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		box.languageManager().remove(language);
		return true;
	}

}
