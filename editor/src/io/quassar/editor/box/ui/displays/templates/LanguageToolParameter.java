package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.model.LanguageTool;

public class LanguageToolParameter extends AbstractLanguageToolParameter<EditorBox> {
	private LanguageTool.Parameter parameter;

	public LanguageToolParameter(EditorBox box) {
		super(box);
	}

	public void parameter(LanguageTool.Parameter parameter) {
		this.parameter = parameter;
	}

	@Override
	public void refresh() {
		super.refresh();
		name.value(parameter.name() + ":");
		value.value(parameter.value());
	}
}