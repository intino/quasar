package io.intino.ime.box.commands.language;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;

import java.time.Instant;

public class SaveLanguageCommand extends Command<Boolean> {
	public Language language;
	public String builderUrl;

	public SaveLanguageCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		language.builderUrl(builderUrl);
		box.languageManager().save(language);
		return true;
	}

}
