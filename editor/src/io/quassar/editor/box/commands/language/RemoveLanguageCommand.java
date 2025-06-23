package io.quassar.editor.box.commands.language;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;
import org.apache.commons.io.serialization.ClassNameMatcher;

import java.util.List;

public class RemoveLanguageCommand extends Command<Boolean> {
	public Language language;

	public RemoveLanguageCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		List<Model> models = box.modelManager().models(language);
		models.forEach(m -> box.modelManager().remove(m));
		box.languageManager().remove(language);
		return true;
	}

}
