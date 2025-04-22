package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.model.LanguageTool;

import java.util.function.Consumer;

public class LanguageToolTemplate extends AbstractLanguageToolTemplate<EditorBox> {
	private LanguageTool tool;
	private Consumer<LanguageTool> removeListener;

	public LanguageToolTemplate(EditorBox box) {
		super(box);
	}

	public void tool(LanguageTool tool) {
		this.tool = tool;
	}

	public void onRemove(Consumer<LanguageTool> listener) {
		this.removeListener = listener;
	}

	@Override
	public void init() {
		super.init();
		removeLink.onExecute(e -> removeListener.accept(tool));
	}

	@Override
	public void refresh() {
		super.refresh();
		name.value(tool.name());
		parameters.clear();
		tool.parameters().forEach(p -> fill(p, parameters.add()));
	}

	private void fill(LanguageTool.Parameter parameter, LanguageToolParameter display) {
		display.parameter(parameter);
		display.refresh();
	}
}