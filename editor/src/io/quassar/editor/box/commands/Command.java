package io.quassar.editor.box.commands;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.model.Model;

import java.time.Clock;
import java.time.Instant;

public abstract class Command<T> {
	public final EditorBox box;
	public String author;
	public Instant ts;

	private static final String EventSource = "UI";

	public Command(EditorBox box) {
		this.box = box;
	}

	public abstract T execute();

	protected static Instant ts() {
		return Instant.now(Clock.systemUTC());
	}

	protected String author() {
		return this.author != null ? this.author : Model.DefaultOwner;
	}

}