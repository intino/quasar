package io.quassar.editor.box.commands;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.user.BuyLicenseTimeCommand;
import io.quassar.editor.box.commands.user.CreateUserCommand;
import io.quassar.editor.model.User;

public class UserCommands extends Commands {

	public UserCommands(EditorBox box) {
		super(box);
	}

	public User create(String username, String author) {
		CreateUserCommand command = setup(new CreateUserCommand(box), author);
		command.username = username;
		return command.execute();
	}

	public User buy(int licenseTime, String author) {
		BuyLicenseTimeCommand command = setup(new BuyLicenseTimeCommand(box), author);
		command.licenseTime = licenseTime;
		return command.execute();
	}

}
