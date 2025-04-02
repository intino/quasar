package io.quassar.editor.box.commands.language;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.model.Language;

import java.io.File;

public class PublishLanguageCommand extends Command<Language> {
	public String name;
	public String version;
	public Language.Level level;

	public static final String MavenDslFile = "%s/%s/%s-%s.jar";

	public PublishLanguageCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Language execute() {
		File dsl = LanguageHelper.mavenDslFile(name, version, box);
		Language language = box.languageManager().get(name);
		box.languageManager().saveDsl(language, dsl);
		language.version(version);
		language.level(level);
		box.languageManager().save(language);
		return language;
	}

}
