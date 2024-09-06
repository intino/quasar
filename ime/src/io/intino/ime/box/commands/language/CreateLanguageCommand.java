package io.intino.ime.box.commands.language;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;

import java.time.Instant;

public class CreateLanguageCommand extends Command<Language> {
	public Model model;
	public String version;

	public CreateLanguageCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Language execute() {
		String name = model.name();
		return box.languageManager().create(model, name, version, author, Instant.now());
	}

}
