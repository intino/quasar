package io.quassar.editor.box.commands.user;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.models.File;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.box.util.ShortIdGenerator;
import io.quassar.editor.model.Model;
import io.quassar.editor.model.User;

import java.io.InputStream;

public class CreateUserCommand extends Command<User> {
	public String username;

	public CreateUserCommand(EditorBox box) {
		super(box);
	}

	@Override
	public User execute() {
		return box.userManager().create(ShortIdGenerator.generate(), username);
	}

}
