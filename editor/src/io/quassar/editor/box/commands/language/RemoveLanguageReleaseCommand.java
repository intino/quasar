package io.quassar.editor.box.commands.language;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import io.quassar.editor.model.Model;

import java.util.List;

public class RemoveLanguageReleaseCommand extends Command<Boolean> {
	public Language language;
	public String release;

	public RemoveLanguageReleaseCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		LanguageRelease release = language.release(this.release);
		box.modelManager().remove(release.template());
		release.examples().forEach(e -> box.modelManager().remove(e));
		box.languageManager().removeRelease(language, release);
		return true;
	}

}
