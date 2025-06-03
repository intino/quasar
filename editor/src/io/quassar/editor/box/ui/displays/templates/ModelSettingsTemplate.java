package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.model.Model;

import java.util.function.Consumer;

public class ModelSettingsTemplate extends AbstractModelSettingsTemplate<EditorBox> {
	private Model model;
	private String release;

	public ModelSettingsTemplate(EditorBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
	}

	public void release(String release) {
		this.release = release;
	}

	public void onSaveTitle(Consumer<Model> listener) {
		modelSettingsEditor.onSaveTitle(listener);
	}

	public void onClone(Consumer<Model> listener) {
		modelSettingsEditor.onClone(listener);
	}

	public void onUpdateLanguageVersion(Consumer<Model> listener) {
		modelSettingsEditor.onUpdateLanguageVersion(listener);
	}

	@Override
	public void refresh() {
		super.refresh();
		modelSettingsEditor.model(model);
		modelSettingsEditor.release(release);
		modelSettingsEditor.refresh();
	}

}