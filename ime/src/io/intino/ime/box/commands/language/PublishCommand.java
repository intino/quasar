package io.intino.ime.box.commands.language;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.model.LanguageLevel;
import io.intino.ime.model.Model;
import io.intino.ime.model.Release;

public class PublishCommand extends Command<Release> {
	public Model model;
	public LanguageLevel level;
	public String version;

	public PublishCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Release execute() {
		return box.languageManager().createRelease(model, level, version);
	}

}
