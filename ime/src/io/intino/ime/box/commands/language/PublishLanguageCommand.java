package io.intino.ime.box.commands.language;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.model.Language;

public class PublishLanguageCommand extends Command<Language> {
	public Language language;

	public PublishLanguageCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Language execute() {
		language.isPrivate(false);
		return box.languageManager().save(language);
	}

}
