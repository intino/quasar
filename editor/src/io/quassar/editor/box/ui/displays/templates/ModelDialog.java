package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.ModelCommands;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.util.List;
import java.util.function.Consumer;

public class ModelDialog extends AbstractModelDialog<EditorBox> {
	private Language language;
	private Consumer<Model> createListener;

	public ModelDialog(EditorBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void onCreate(Consumer<Model> listener) {
		this.createListener = listener;
	}

	public void open() {
		dialog.open();
	}

	@Override
	public void init() {
		super.init();
		dialog.onOpen(e -> refreshDialog());
		create.onExecute(e -> create());
		nameField.onChange(e -> checkName());
		titleField.onEnterPress(e -> create());
	}

	private void refreshDialog() {
		languageTitle.value(language.name());
		nameField.value(ModelHelper.proposeName());
		titleField.value("(no name)");
		descriptionField.value("(no description)");
	}

	private void create() {
		if (!check()) return;
		dialog.close();
		String name = nameField.value();
		String title = titleField.value();
		String description = descriptionField.value();
		Model model = box().commands(ModelCommands.class).create(name, title, description, language, DisplayHelper.user(session()), username());
		createListener.accept(model);
	}

	private boolean check() {
		return checkName() &&
			   DisplayHelper.check(titleField, this::translate) &&
			   DisplayHelper.check(descriptionField, this::translate);
	}

	private boolean checkName() {
		return DisplayHelper.checkLanguageName(nameField, language, this::translate, box());
	}

}