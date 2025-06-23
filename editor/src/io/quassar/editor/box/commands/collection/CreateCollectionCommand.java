package io.quassar.editor.box.commands.collection;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.util.LicenseGenerator;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.model.Collection;

import java.util.stream.IntStream;

public class CreateCollectionCommand extends Command<Collection> {
	public String name;
	public Collection.SubscriptionPlan plan;

	public CreateCollectionCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Collection execute() {
		return box.collectionManager().create(name, plan, author());
	}

}
