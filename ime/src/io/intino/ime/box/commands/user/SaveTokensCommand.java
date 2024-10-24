package io.intino.ime.box.commands.user;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.box.users.TokenProvider;

public class SaveTokensCommand extends Command<Boolean> {
	public TokenProvider.Record tokens;

	public SaveTokensCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		box.tokenProvider().save(tokens);
		return true;
	}

}
