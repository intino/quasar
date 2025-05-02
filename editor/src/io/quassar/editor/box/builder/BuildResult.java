package io.quassar.editor.box.builder;

import io.intino.alexandria.Resource;
import io.intino.builderservice.schemas.Message;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public record BuildResult(List<Message> messages, Resource zipArtifacts) {

	public boolean success() {
		return messages.isEmpty() || messages.stream().allMatch(m -> m.kind() != Message.Kind.ERROR);
	}

	public static BuildResult success(List<Message> messages, Resource zipArtifacts) {
		return new BuildResult(Collections.emptyList(), zipArtifacts);
	}

	public static BuildResult failure(List<Message> messages) {
		return new BuildResult(messages, null);
	}

}
