package io.quassar.editor.box.commands.collection;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Collection;
import io.quassar.editor.model.License;

import java.util.List;

public class AddLicenseCommand extends Command<License> {
	public Collection collection;
	public int duration;

	public AddLicenseCommand(EditorBox box) {
		super(box);
	}

	@Override
	public License execute() {
		AddLicensesCommand command = new AddLicensesCommand(box);
		command.collection = collection;
		command.count = 1;
		command.duration = duration;
		command.author = author;
		List<License> result = command.execute();
		if (result.isEmpty()) return null;
		box.collectionManager().assign(result.getFirst(), author);
		return result.getFirst();
	}

}
