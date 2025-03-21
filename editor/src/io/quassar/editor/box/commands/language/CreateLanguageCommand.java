package io.quassar.editor.box.commands.language;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Language;

import java.io.File;

public class CreateLanguageCommand extends Command<Language> {
	public String name;
	public String parent;
	public Language.Level level;
	public String description;
	public File logo;

	public CreateLanguageCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Language execute() {
		File dsl = null; // TODO Llamar al builder service para crear el lenguaje
		return box.languageManager().create(name, level, description, null, logo, parent, author());
	}

}
