package io.quassar.editor.box.commands.collection;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.util.LicenseGenerator;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.box.util.UserHelper;
import io.quassar.editor.model.Collection;
import io.quassar.editor.model.User;

import java.util.stream.IntStream;

public class AddLicensesCommand extends Command<Boolean> {
	public Collection collection;
	public int count;
	public int duration;

	public AddLicensesCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		if (!PermissionsHelper.hasCredit(count*duration, author, box)) return false;
		IntStream.range(0, count).forEach(i -> box.collectionManager().createLicense(collection, LicenseGenerator.generate(), duration));
		User user = UserHelper.user(author, box);
		int licenseTime = UserHelper.licenseTime(author, box);
		user.licenseTime(licenseTime - count*duration);
		return true;
	}

}
