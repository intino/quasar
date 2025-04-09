package io.quassar.editor.box.commands;

import io.intino.builderservice.schemas.Message;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.builder.CheckResult;
import io.quassar.editor.model.OperationResult;
import io.quassar.editor.model.User;

import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
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

	protected ExecutionResult resultOf(OperationResult result) {
		return resultOf(result, null);
	}

	protected ExecutionResult resultOf(OperationResult result, InputStream output) {
		return ExecutionResult.check(List.of(new Message().kind(result.success() ? Message.Kind.INFO : Message.Kind.ERROR).content(result.message())), output);
	}

	public interface ExecutionResult {
		boolean success();
		List<Message> messages();
		InputStream output();

		static ExecutionResult check(CheckResult result) {
			return new ExecutionResult() {
				@Override
				public boolean success() {
					return result.messages().stream().noneMatch(m -> m.kind() == Message.Kind.ERROR);
				}

				@Override
				public List<Message> messages() {
					return result.messages();
				}

				@Override
				public InputStream output() {
					return result.output();
				}
			};
		}

		static ExecutionResult check(List<Message> messages) {
			return check(messages, null);
		}

		static ExecutionResult check(List<Message> messages, InputStream output) {
			return new ExecutionResult() {
				@Override
				public boolean success() {
					return messages.stream().noneMatch(m -> m.kind() == Message.Kind.ERROR);
				}

				@Override
				public List<Message> messages() {
					return messages;
				}

				@Override
				public InputStream output() {
					return output;
				}
			};
		}
	}

}