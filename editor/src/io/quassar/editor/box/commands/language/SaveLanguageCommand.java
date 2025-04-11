package io.quassar.editor.box.commands.language;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Language;

import java.io.File;
import java.util.List;

public class SaveLanguageCommand extends Command<Boolean> {
	public Language language;
	public String title;
	public String description;
	public Language.Level level;
	public List<String> tags;
	public File logo;

	public SaveLanguageCommand(EditorBox box) {
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
		language.level(level);
		language.tags(tags);
		box.languageManager().saveLogo(language, logo);
	}

}
