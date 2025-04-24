package io.quassar.editor.box.commands;

import io.quassar.editor.box.EditorBox;
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

}
