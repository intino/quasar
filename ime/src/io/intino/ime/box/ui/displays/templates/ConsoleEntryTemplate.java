package io.intino.ime.box.ui.displays.templates;

import io.intino.builderservice.schemas.Message;
import io.intino.ime.box.ImeBox;

public class ConsoleEntryTemplate extends AbstractConsoleEntryTemplate<ImeBox> {
	private Message message;

	public ConsoleEntryTemplate(ImeBox box) {
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