package io.quassar.editor.box.ui.displays;

import io.quassar.editor.box.EditorBox;

public class EditorHelpDisplay extends AbstractEditorHelpDisplay<EditorBox> {

	public EditorHelpDisplay(EditorBox box) {
		super(box);
	}

	@Override
	public void refresh() {
		super.refresh();
		notifier.refresh();
	}
}