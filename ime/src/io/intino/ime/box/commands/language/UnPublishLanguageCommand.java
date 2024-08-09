package io.intino.ime.box.commands.language;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.model.Language;

public class UnPublishLanguageCommand extends Command<Language> {
	public Language language;

	public UnPublishLanguageCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Language execute() {
		language.isPrivate(true);
		return box.languageManager().save(language);
	}

}
