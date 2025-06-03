package io.quassar.editor.box.commands.language;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.ui.displays.templates.CreateLanguageEditor;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageExecution;
import io.quassar.editor.model.LanguageRelease;

public class CreateLanguageReleaseExecutionCommand extends Command<LanguageExecution> {
	public Language language;
	public String release;
	public String name;
	public LanguageExecution.Type type;

	public CreateLanguageReleaseExecutionCommand(EditorBox box) {
		super(box);
	}

	@Override
	public LanguageExecution execute() {
		LanguageRelease release = language.release(this.release);
		if (release.execution() != null) release.execution().type(type);
		else box.languageManager().createExecution(language, release, name, type);
		return release.execution();
	}

}
