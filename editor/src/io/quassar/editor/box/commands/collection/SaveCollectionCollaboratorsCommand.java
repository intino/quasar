package io.quassar.editor.box.commands.collection;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.model.Collection;
import io.quassar.editor.model.Model;

import java.util.List;

public class SaveCollectionCollaboratorsCommand extends Command<Boolean> {
	public Collection collection;
	public List<String> collaborators;

	public SaveCollectionCollaboratorsCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		List<String> collaboratorList = collaborators.subList(0, Math.min(collaborators.size(), Integer.parseInt(box.configuration().collectionCollaboratorsCount())));
		collection.collaborators(collaboratorList);
		return true;
	}

}
