package io.quassar.editor.box.commands.language;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageExecution;
import io.quassar.editor.model.LanguageRelease;

public class SaveLanguageReleaseExecutionPropertiesCommand extends Command<Boolean> {
	public Language language;
	public String release;
	public String installationUrl;

	public SaveLanguageReleaseExecutionPropertiesCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		LanguageRelease release = language.release(this.release);
		LanguageExecution execution = release.execution();
		execution.installationUrl(installationUrl);
		return true;
	}

}
