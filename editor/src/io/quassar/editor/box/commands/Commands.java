package io.quassar.editor.box.commands;

import io.quassar.editor.box.EditorBox;

import java.time.Clock;
import java.time.Instant;

public class Commands {
	protected final EditorBox box;

	public Commands(EditorBox box) {
		this.box = box;
	}

	protected <C extends Command<?>> C setup(C command, String author) {
		command.author = author;
		command.ts = Instant.now(Clock.systemUTC());
		return command;
	}

}
