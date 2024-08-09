package io.intino.ime.box.commands.language;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.model.Language;
import io.intino.ime.model.User;
import io.intino.ime.model.Workspace;

import java.time.Instant;

public class CreateLanguageCommand extends Command<Language> {
	public Workspace workspace;
	public String version;

	public CreateLanguageCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Language execute() {
		String name = workspace.name();
		return box.languageManager().create(workspace, name, version, author, Instant.now());
	}

}
