package io.quassar.editor.box.builder;

import io.intino.builderservice.schemas.Message;

import java.io.InputStream;
import java.util.List;

import static java.util.Collections.emptyList;

public record CheckResult(String ticket, List<Message> messages) {

	public boolean success() {
		return messages.isEmpty() || messages.stream().allMatch(m -> m.kind() != Message.Kind.ERROR);
	}

	public static CheckResult success(String ticket, List<Message> messages) {
		return new CheckResult(ticket, messages);
	}

	public static CheckResult failure(String ticket, List<Message> messages) {
		return new CheckResult(ticket, messages);
	}

	public static CheckResult failure(Message message) {
		return new CheckResult(null, List.of(message));
	}

}
