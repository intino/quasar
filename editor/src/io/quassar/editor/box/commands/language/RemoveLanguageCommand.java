package io.quassar.editor.box.commands.language;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Language;

public class RemoveLanguageCommand extends Command<Boolean> {
	public Language language;

	public RemoveLanguageCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		box.languageManager().remove(language);
		return true;
	}

}
