package io.quassar.editor.box.commands.language;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Language;

public class SaveLanguagePropertiesCommand extends Command<Boolean> {
	public Language language;
	public String title;
	public String description;

	public SaveLanguagePropertiesCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		saveLanguage();
		return true;
	}

	private void saveLanguage() {
		language.title(title);
		language.description(description);
		box.languageManager().save(language);
	}

}
