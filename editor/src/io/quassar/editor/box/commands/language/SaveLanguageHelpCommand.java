package io.quassar.editor.box.commands.language;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;

public class SaveLanguageHelpCommand extends Command<Boolean> {
	public Language language;
	public String release;
	public String content;

	public SaveLanguageHelpCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		if (content == null) return false;
		box.languageManager().saveHelp(language, release, content);
		return true;
	}

}
