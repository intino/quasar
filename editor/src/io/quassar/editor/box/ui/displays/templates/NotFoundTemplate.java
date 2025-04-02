package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;

public class NotFoundTemplate extends AbstractNotFoundTemplate<EditorBox> {
	private String type;

	public NotFoundTemplate(EditorBox box) {
		super(box);
	}

	public void type(String type) {
		this.type = type;
	}

	@Override
	public void refresh() {
		super.refresh();
		message.value(String.format(translate("%s was not found"), type));
	}

}