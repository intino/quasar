package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;

import java.util.function.Consumer;

public class TokenEditor extends AbstractTokenEditor<EditorBox> {
	private String app;
	private Consumer<String> removeListener;

	public TokenEditor(EditorBox box) {
		super(box);
	}

	public void app(String value) {
		this.app = value;
	}

	public void onRemove(Consumer<String> listener) {
		this.removeListener = listener;
	}

	@Override
	public void init() {
		super.init();
		delete.onExecute(e -> removeListener.accept(app));
	}

	@Override
	public void refresh() {
		super.refresh();
		appField.value(app);
	}

}