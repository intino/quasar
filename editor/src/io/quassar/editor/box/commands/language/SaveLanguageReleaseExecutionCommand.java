package io.quassar.editor.box.commands.language;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageExecution;
import io.quassar.editor.model.LanguageRelease;

public class SaveLanguageReleaseExecutionCommand extends Command<Boolean> {
	public Language language;
	public String release;
	public LanguageExecution.Type type;

	public SaveLanguageReleaseExecutionCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		LanguageRelease release = language.release(this.release);
		if (release.execution() == null) box.languageManager().createExecution(language, release, type);
		else release.execution().type(type);
		return true;
	}

}
