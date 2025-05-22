package io.quassar.editor.box.commands;

import io.intino.builderservice.schemas.Message;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.builder.BuildResult;
import io.quassar.editor.box.builder.CheckResult;
import io.quassar.editor.model.OperationResult;
import io.quassar.editor.model.User;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

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
		return this.author != null ? this.author : User.Anonymous;
	}

	protected CommandResult resultOf(OperationResult result) {
		if (result.message() == null) return CommandResult.check(Collections.emptyList());
		return CommandResult.check(List.of(new Message().kind(result.success() ? Message.Kind.INFO : Message.Kind.ERROR).content(result.message())));
	}

	protected CommandResult resultOf(BuildResult result) {
		return new CommandResult() {
			@Override
			public boolean success() {
				return result.success();
			}

			@Override
			public List<Message> messages() {
				return result.messages();
			}
		};
	}

	public interface CommandResult {
		boolean success();
		List<Message> messages();

		static CommandResult check(CheckResult result) {
			return check(result.messages());
		}

		static CommandResult check(List<Message> messages) {
			return new CommandResult() {
				@Override
				public boolean success() {
					return messages.stream().noneMatch(m -> m.kind() == Message.Kind.ERROR);
				}

				@Override
				public List<Message> messages() {
					return messages;
				}
			};
		}
	}

}