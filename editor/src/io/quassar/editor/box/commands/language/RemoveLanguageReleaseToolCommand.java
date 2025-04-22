package io.quassar.editor.box.commands.language;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import io.quassar.editor.model.LanguageTool;

import java.util.Map;

public class RemoveLanguageReleaseToolCommand extends Command<Boolean> {
	public Language language;
	public String release;
	public LanguageTool tool;

	public RemoveLanguageReleaseToolCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		LanguageRelease release = language.release(this.release);
		return box.languageManager().removeReleaseTool(language, release, tool);
	}

}
