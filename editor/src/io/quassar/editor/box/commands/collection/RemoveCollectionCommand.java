package io.quassar.editor.box.commands.collection;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Collection;
import io.quassar.editor.model.Language;

import java.util.List;

public class RemoveCollectionCommand extends Command<Boolean> {
	public Collection collection;

	public RemoveCollectionCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		List<Language> languages = box.languageManager().languages(collection);
		if (!languages.isEmpty()) return false;
		box.collectionManager().remove(collection, author());
		return true;
	}

}
