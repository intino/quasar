package io.quassar.editor.box.commands.language;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Language;

import java.util.List;

public class MakeLanguagePrivateCommand extends Command<Boolean> {
	public Language language;
	public List<String> accessPatterns;

	public MakeLanguagePrivateCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		box.languageManager().makePrivate(language, accessPatterns);
		return true;
	}

}
