package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.model.Model;

public class CreateLanguageEditor extends AbstractCreateLanguageEditor<EditorBox> {
	private Model metamodel;

	public CreateLanguageEditor(EditorBox box) {
		super(box);
	}

	public void model(Model metamodel) {
		this.metamodel = metamodel;
	}

	@Override
	public void init() {
		super.init();
		nameField.onChange(e -> DisplayHelper.checkLanguageName(nameField, this::translate, box()));
		create.onExecute(e -> createLanguage());
	}

	@Override
	public void refresh() {
		super.refresh();
		metamodelTitle.value(ModelHelper.label(metamodel, language(), box()));
	}

	private void createLanguage() {
		voy por aqui
	}
}