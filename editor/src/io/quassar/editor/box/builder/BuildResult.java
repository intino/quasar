package io.quassar.editor.box.builder;

import io.intino.builderservice.schemas.Message;

import java.io.InputStream;
import java.util.List;

public record BuildResult(List<Message> messages, InputStream output) {

	public boolean success() {
		return messages.stream().allMatch(m -> m.kind() != Message.Kind.ERROR);
	}

	public static BuildResult success(InputStream output, Message... messages) {
		return success(output, List.of(messages));
	}

	public static BuildResult success(InputStream output, List<Message> messages) {
		return new BuildResult(messages, output);
	}

	public static BuildResult failure(List<Message> messages) {
		return new BuildResult(messages, null);
	}

	public static BuildResult failure(Message... messages) {
		return failure(List.of(messages));
	}

}
