package io.intino.ime.box.commands.language;

import io.intino.alexandria.Resource;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.model.Language;
import io.intino.ime.model.Operation;

import java.util.List;

public class SaveLanguageCommand extends Command<Boolean> {
	public Language language;
	public String description;
	public boolean isPrivate;
	public String builder;
	public Resource logo;
	public List<Operation> operations;
	public List<String> tags;

	public SaveLanguageCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		language.description(description);
		language.isPrivate(isPrivate);
		language.builder(builder);
		language.operations(operations);
		language.tags(tags);
		box.languageManager().save(language);
		if (logo != null) box.languageManager().saveLogo(language, logo);
		return true;
	}

}
