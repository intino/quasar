package io.quassar.editor.box.commands.language;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.io.File;
import java.util.UUID;

public class CreateLanguageCommand extends Command<Language> {
	public String id;
	public Model metamodel;
	public Language.Level level;
	public File logo;

	public CreateLanguageCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Language execute() {
		Language language = box.languageManager().create(Language.groupFrom(id), Language.nameFrom(id), metamodel, level, "", "");
		metamodel.title(Language.nameFrom(id).toUpperCase());
		box.languageManager().saveLogo(language, logo(language));
		return language;
	}

	private File logo(Language language) {
		if (logo != null) return logo;
		File file = new File(box.archetype().tmp().root(), UUID.randomUUID().toString());
		LanguageHelper.generateLogo(language.name(), file);
		return file;
	}

}
