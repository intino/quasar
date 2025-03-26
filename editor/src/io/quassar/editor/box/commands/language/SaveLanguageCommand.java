package io.quassar.editor.box.commands.language;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Language;

import java.io.File;
import java.util.List;

public class SaveLanguageCommand extends Command<Boolean> {
	public Language language;
	public String description;
	public String fileExtension;
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
		language.description(description);
		language.fileExtension(fileExtension);
		language.level(level);
		language.tags(tags);
		box.languageManager().save(language);
		if (logo != null) box.languageManager().saveLogo(language, logo);
	}

}
