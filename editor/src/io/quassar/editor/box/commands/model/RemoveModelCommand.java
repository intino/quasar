package io.quassar.editor.box.commands.model;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.commands.language.RemoveLanguageCommand;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import io.quassar.editor.model.Model;

public class RemoveModelCommand extends Command<Boolean> {
	public Model model;

	public RemoveModelCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		Language language = box.languageManager().get(model);
		if (language != null && ModelHelper.isMetamodel(model, box)) removeLanguage(language);
		unlinkModel();
		removeModel();
		return true;
	}

	private void removeLanguage(Language language) {
		RemoveLanguageCommand command = new RemoveLanguageCommand(box);
		command.author = author;
		command.language = language;
		command.execute();
	}

	private void unlinkModel() {
		if (!model.isExample()) return;
		Language language = box.languageManager().get(model);
		LanguageRelease release = language.release(model.language().version());
		if (release == null) return;
		release.removeExample(model.id());
	}

	private void removeModel() {
		box.modelManager().remove(model);
	}

}
