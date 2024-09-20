package io.intino.ime.box.commands.language;

import io.intino.alexandria.Resource;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;
import io.intino.ime.model.Release;

public class CreateLanguageCommand extends Command<Language> {
	public String name;
	public Release parent;
	public String description;
	public Resource logo;
	public boolean isPrivate;

	public CreateLanguageCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Language execute() {
		Language language = box.languageManager().create(name, description, logo, parent, author(), isPrivate);
		box.modelManager().create(ModelHelper.proposeName(), name, parent, author(), name, isPrivate);
		return language;
	}

}
