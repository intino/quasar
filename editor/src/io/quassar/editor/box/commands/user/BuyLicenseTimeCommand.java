package io.quassar.editor.box.commands.user;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.util.ShortIdGenerator;
import io.quassar.editor.model.User;

public class BuyLicenseTimeCommand extends Command<User> {
	public int licenseTime;

	public BuyLicenseTimeCommand(EditorBox box) {
		super(box);
	}

	@Override
	public User execute() {
		User user = box.userManager().get(author);
		user.licenseTime(user.licenseTime() + licenseTime);
		return user;
	}

}
