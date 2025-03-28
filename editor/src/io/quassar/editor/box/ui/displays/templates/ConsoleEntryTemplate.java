package io.quassar.editor.box.ui.displays.templates;

import io.intino.builderservice.schemas.Message;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.model.Model;

public class ConsoleEntryTemplate extends AbstractConsoleEntryTemplate<EditorBox> {
	private Model model;
	private String release;
	private Message message;

	public ConsoleEntryTemplate(EditorBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
	}

	public void release(String release) {
		this.release = release;
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
		file.title(message.uri());
		file.address(path -> PathHelper.modelPath(path, model, release, message.uri()));
		location.value(message.line() + ":" + message.column());
		content.value(message.content());
	}

}