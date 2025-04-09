package io.quassar.editor.box.builder;

import io.intino.builderservice.schemas.Message;

import java.io.InputStream;
import java.util.List;

public record CheckResult(List<Message> messages, InputStream output) {

	public boolean success() {
		return messages.stream().allMatch(m -> m.kind() != Message.Kind.ERROR);
	}

	public static CheckResult success(InputStream output, Message... messages) {
		return success(output, List.of(messages));
	}

	public static CheckResult success(InputStream output, List<Message> messages) {
		return new CheckResult(messages, output);
	}

	public static CheckResult failure(List<Message> messages) {
		return new CheckResult(messages, null);
	}

	public static CheckResult failure(Message... messages) {
		return failure(List.of(messages));
	}

}
