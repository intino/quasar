package io.quassar.editor.box.commands.language;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Language;

public class MakeLanguagePrivateCommand extends Command<Boolean> {
	public Language language;

	public MakeLanguagePrivateCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		language.isPrivate(true);
		return true;
	}

}
