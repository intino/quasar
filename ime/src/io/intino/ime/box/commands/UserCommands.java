package io.intino.ime.box.commands;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.user.SaveTokensCommand;
import io.intino.ime.box.users.TokenProvider;

public class UserCommands extends Commands {

	public UserCommands(ImeBox box) {
		super(box);
	}

	public void save(TokenProvider.Record tokens, String username) {
		SaveTokensCommand command = setup(new SaveTokensCommand(box), username);
		command.tokens = tokens;
		command.execute();
	}

}
