package io.quassar.editor.box.commands.language;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;

import java.util.List;

public class SaveLanguageAccessCommand extends Command<Boolean> {
	public Language language;
	public List<String> access;

	public SaveLanguageAccessCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		box.languageManager().saveAccess(language, access);
		return true;
	}

}
