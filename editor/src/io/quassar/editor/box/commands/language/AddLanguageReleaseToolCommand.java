package io.quassar.editor.box.commands.language;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import io.quassar.editor.model.LanguageTool;

import java.util.Map;

public class AddLanguageReleaseToolCommand extends Command<LanguageTool> {
	public Language language;
	public String release;
	public String name;
	public LanguageTool.Type type;
	public Map<String, String> parameters;

	public AddLanguageReleaseToolCommand(EditorBox box) {
		super(box);
	}

	@Override
	public LanguageTool execute() {
		LanguageRelease release = language.release(this.release);
		return box.languageManager().createReleaseTool(language, release, name, type, parameters);
	}

}
