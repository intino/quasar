package io.quassar.editor.box.commands;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command.CommandResult;
import io.quassar.editor.box.commands.collection.CreateCollectionCommand;
import io.quassar.editor.box.commands.language.*;
import io.quassar.editor.model.*;

import java.io.File;
import java.util.List;

public class CollectionCommands extends Commands {

	public CollectionCommands(EditorBox box) {
		super(box);
	}

	public Collection create(String name, String username) {
		CreateCollectionCommand command = setup(new CreateCollectionCommand(box), username);
		command.name = name;
		return command.execute();
	}

}
