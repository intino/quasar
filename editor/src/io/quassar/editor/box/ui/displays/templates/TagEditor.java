package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;

import java.util.function.Consumer;

public class TagEditor extends AbstractTagEditor<EditorBox> {
	private String tag;
	private Consumer<String> removeListener;

	public TagEditor(EditorBox box) {
		super(box);
	}

	public void tag(String tag) {
		this.tag = tag;
	}

	public void onRemove(Consumer<String> listener) {
		this.removeListener = listener;
	}

	@Override
	public void init() {
		super.init();
		delete.onExecute(e -> removeListener.accept(tag));
	}

	@Override
	public void refresh() {
		super.refresh();
		nameField.value(tag);
	}

}