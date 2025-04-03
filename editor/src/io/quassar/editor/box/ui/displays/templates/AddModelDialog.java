package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.ModelCommands;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.util.function.Consumer;

public class AddModelDialog extends AbstractAddModelDialog<EditorBox> {
	private Language language;
	private Consumer<Model> createListener;

	public AddModelDialog(EditorBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void onCreate(Consumer<Model> listener) {
		this.createListener = listener;
	}

	public void open() {
		String name = ModelHelper.proposeName();
		create(name, name, translate("(no hint)"), translate("(no description)"));
		//dialog.open();
	}

	@Override
	public void init() {
		super.init();
		dialog.onOpen(e -> refreshDialog());
		create.onExecute(e -> create());
		nameField.onChange(e -> checkName());
		titleField.onEnterPress(e -> create());
		hintField.onEnterPress(e -> create());
	}

	private void refreshDialog() {
		String name = ModelHelper.proposeName();
		dialog.title("Add model with %s".formatted(language.name()));
		nameField.value(name);
		titleField.value(name);
		hintField.value("(no hint)");
		descriptionField.value("(no description)");
	}

	private void create() {
		if (!check()) return;
		dialog.close();
		String name = nameField.value();
		String title = titleField.value();
		String hint = hintField.value();
		String description = descriptionField.value();
		create(name, title, hint, description);
	}

	private void create(String name, String title, String hint, String description) {
		Model model = box().commands(ModelCommands.class).create(name, title, hint, description, language, DisplayHelper.user(session()), username());
		createListener.accept(model);
	}

	private boolean check() {
		return checkName() &&
			   DisplayHelper.check(titleField, this::translate) &&
			   DisplayHelper.check(hintField, this::translate) &&
			   DisplayHelper.check(descriptionField, this::translate);
	}

	private boolean checkName() {
		return DisplayHelper.checkLanguageName(nameField, language, this::translate, box());
	}

}