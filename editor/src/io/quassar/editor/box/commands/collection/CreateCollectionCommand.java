package io.quassar.editor.box.commands.collection;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Collection;

public class CreateCollectionCommand extends Command<Collection> {
	public String name;

	public CreateCollectionCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Collection execute() {
		return box.collectionManager().create(name, author());
	}

}
