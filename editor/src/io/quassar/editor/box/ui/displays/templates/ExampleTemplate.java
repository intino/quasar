package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;

import java.util.function.Consumer;

public class ExampleTemplate extends AbstractExampleTemplate<EditorBox> {
	private String templateLanguage;
	private Consumer<String> clickListener;

	public ExampleTemplate(EditorBox box) {
		super(box);
	}

	public void templateLanguage(String language) {
		this.templateLanguage = language;
	}

	public void onClick(Consumer<String> listener) {
		this.clickListener = listener;
	}

	@Override
	public void init() {
		super.init();
		link.onExecute(e -> clickListener.accept(templateLanguage));
	}

	@Override
	public void refresh() {
		super.refresh();
		link.title(templateLanguage);
	}

}