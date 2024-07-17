package io.intino.languageeditor.box.commands;

import io.intino.languageeditor.box.LanguageEditorBox;

import java.time.Clock;
import java.time.Instant;

public class Commands {
	protected final LanguageEditorBox box;

	public Commands(LanguageEditorBox box) {
		this.box = box;
	}

	protected <C extends Command> C setup(C command, String author) {
		command.author = author;
		command.ts = Instant.now(Clock.systemUTC());
		return command;
	}

}
