package io.quassar.editor.box.commands.language;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageExecution;
import io.quassar.editor.model.LanguageRelease;

public class SaveLanguageReleaseExecutionCommand extends Command<Boolean> {
	public Language language;
	public String release;
	public String name;
	public LanguageExecution.Type type;
	public String content;

	public SaveLanguageReleaseExecutionCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		LanguageRelease release = language.release(this.release);
		LanguageExecution execution = release.execution();
		if (execution == null) execution = box.languageManager().createExecution(language, release, name, type);
		execution.name(name);
		execution.type(type);
		execution.content(content);
		return true;
	}

}
