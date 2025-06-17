package io.quassar.editor.box.commands.language;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.commands.LanguageCommands;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.model.Collection;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.io.File;
import java.util.UUID;

public class CreateLanguageCommand extends Command<Language> {
	public Collection collection;
	public String name;
	public Model metamodel;
	public Language.Level level;
	public boolean isPrivate;
	public File logo;

	public CreateLanguageCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Language execute() {
		Language language = box.languageManager().create(collection, name, metamodel, level, "", "");
		language.isPrivate(isPrivate);
		metamodel.title(name.toUpperCase());
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
