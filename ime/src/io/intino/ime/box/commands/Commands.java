package io.intino.ime.box.commands;

import io.intino.ime.box.ImeBox;

import java.time.Clock;
import java.time.Instant;

public class Commands {
	protected final ImeBox box;

	public Commands(ImeBox box) {
		this.box = box;
	}

	protected <C extends Command> C setup(C command, String author) {
		command.author = author;
		command.ts = Instant.now(Clock.systemUTC());
		return command;
	}

}
