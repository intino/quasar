package io.quassar.editor.box.commands.language;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageExecution;
import io.quassar.editor.model.LanguageRelease;

public class SaveLanguageReleaseLocalExecutionCommand extends Command<Boolean> {
	public Language language;
	public String release;
	public LanguageExecution.LocalLanguage localLanguage;
	public String content;

	public SaveLanguageReleaseLocalExecutionCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		LanguageRelease release = language.release(this.release);
		release.execution().type(LanguageExecution.Type.Local);
		release.execution().localConfiguration(localLanguage, content);
		return true;
	}

}
