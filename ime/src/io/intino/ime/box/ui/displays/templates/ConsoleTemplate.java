package io.intino.ime.box.ui.displays.templates;

import io.intino.builderservice.schemas.Message;
import io.intino.ime.box.ImeBox;

import java.util.List;
import java.util.function.Consumer;

public class ConsoleTemplate extends AbstractConsoleTemplate<ImeBox> {
	private List<Message> messageList;
	private Consumer<Boolean> closeListener;

	public ConsoleTemplate(ImeBox box) {
		super(box);
	}

	public void messages(List<Message> messages) {
		this.messageList = messages;
	}

	public void onClose(Consumer<Boolean> listener) {
		this.closeListener = listener;
	}

	@Override
	public void init() {
		super.init();
		closeButton.onExecute(e -> closeListener.accept(true));
	}

	@Override
	public void refresh() {
		super.refresh();
		entries.clear();
		messageList.forEach(m -> fill(m, entries.add()));
	}

	private void fill(Message message, ConsoleEntryTemplate display) {
		display.message(message);
		display.refresh();
	}
}