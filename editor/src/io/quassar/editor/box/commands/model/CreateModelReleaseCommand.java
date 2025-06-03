package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.commands.Command.CommandResult;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import io.quassar.editor.model.Model;

public class CreateModelReleaseCommand extends Command<CommandResult> {
	public Model model;
	public String version;

	public CreateModelReleaseCommand(EditorBox box) {
		super(box);
	}

	@Override
	public CommandResult execute() {
		CommandResult result = check();
		if (!result.success()) return result;
		if (box.modelManager().release(model, version).exists()) removeRelease();
		return resultOf(box.modelManager().createRelease(model, version));
	}

	private void removeRelease() {
		box.modelManager().removeRelease(model, version);
		Language language = box.languageManager().getWithMetamodel(model);
		if (language == null) return;
		LanguageRelease release = language.release(version);
		if (release == null) return;
		box.languageManager().removeRelease(language, release);
	}

	private CommandResult check() {
		CheckModelCommand command = new CheckModelCommand(box);
		command.author = author;
		command.model = model;
		command.release = Model.DraftRelease;
		return command.execute();
	}

}
