package io.quassar.editor.box.commands.collection;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Collection;
import io.quassar.editor.model.License;

public class RevokeLicenseCommand extends Command<Boolean> {
	public Collection collection;
	public License license;

	public RevokeLicenseCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		license.status(License.Status.Revoked);
		license.duration(0);
		return true;
	}

}
