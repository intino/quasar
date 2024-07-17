package io.intino.languageeditor.box.commands;

import io.intino.languageeditor.box.LanguageEditorBox;

import java.time.Clock;
import java.time.Instant;

public abstract class Command<T> {
	public final LanguageEditorBox box;
	public String author;
	public Instant ts;

	private static final String EventSource = "UI";

	public Command(LanguageEditorBox box) {
		this.box = box;
	}

	public abstract T execute();

	protected static Instant ts() {
		return Instant.now(Clock.systemUTC());
	}

	protected String author() {
		return this.author != null ? this.author : "anonymous";
	}

}