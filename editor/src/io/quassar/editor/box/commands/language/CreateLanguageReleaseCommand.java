package io.quassar.editor.box.commands.language;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.model.Language;

import java.io.File;

public class CreateLanguageReleaseCommand extends Command<Language> {
	public Language language;
	public String version;
	public Language.Level level;

	public CreateLanguageReleaseCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Language execute() {
		File dsl = LanguageHelper.mavenDslFile(language, version, box);
		box.languageManager().saveDsl(language, version, dsl);
		language.level(level);
		box.languageManager().save(language);
		return language;
	}

}
