package io.quassar.editor.box.commands.language;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.io.File;

public class CreateLanguageCommand extends Command<Language> {
	public String name;
	public Model metamodel;
	public Language.Level level;
	public File logo;

	public CreateLanguageCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Language execute() {
		String group = box.userManager().get(author).id();
			Language language = box.languageManager().create(group, name, metamodel, level, metamodel.title(), metamodel.description());
		box.languageManager().saveLogo(language, logo);
		return language;
	}

}
