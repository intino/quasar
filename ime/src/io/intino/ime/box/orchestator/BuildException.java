package io.intino.ime.box.orchestator;

import io.intino.builderservice.schemas.Message;

import java.util.List;

public class BuildException extends Exception {

	private final List<Message> messages;

	public BuildException(List<Message> messages) {
		this.messages = messages;
	}

	public List<Message> messages() {
		return messages;}
}
