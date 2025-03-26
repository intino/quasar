package io.quassar.editor.box.ui.displays.templates;

import io.intino.builderservice.schemas.Message;
import io.quassar.editor.box.EditorBox;

public class ConsoleEntryTemplate extends AbstractConsoleEntryTemplate<EditorBox> {
	private Message message;

	public ConsoleEntryTemplate(EditorBox box) {
		super(box);
	}

	public void message(Message message) {
		this.message = message;
	}

	@Override
	public void refresh() {
		super.refresh();
		infoIcon.visible(message.kind() == Message.Kind.INFO);
		warningIcon.visible(message.kind() == Message.Kind.WARNING);
		errorIcon.visible(message.kind() == Message.Kind.ERROR);
		file.value(message.uri());
		location.value(message.line() + ":" + message.column());
		content.value(message.content());
	}

}