package io.quassar.editor.box.ui.displays.templates;

import io.intino.builderservice.schemas.Message;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.model.Model;

import java.util.List;
import java.util.function.Consumer;

public class ConsoleTemplate extends AbstractConsoleTemplate<EditorBox> {
	private Model model;
	private String release;
	private List<Message> messageList;
	private Consumer<Boolean> closeListener;

	public ConsoleTemplate(EditorBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
	}

	public void release(String release) {
		this.release = release;
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
		display.model(model);
		display.release(release);
		display.message(message);
		display.refresh();
	}

}