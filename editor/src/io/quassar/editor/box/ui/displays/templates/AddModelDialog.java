package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.ModelCommands;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.model.GavCoordinates;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
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
		create(name, name, translate("(no description)"));
		//dialog.open();
	}

	@Override
	public void init() {
		super.init();
		dialog.onOpen(e -> refreshDialog());
		create.onExecute(e -> create());
		titleField.onEnterPress(e -> create());
	}

	private void refreshDialog() {
		dialog.title("Add model with %s".formatted(language.name()));
		titleField.value(ModelHelper.proposeName());
		descriptionField.value("(no description)");
	}

	private void create() {
		if (!check()) return;
		dialog.close();
		String title = titleField.value();
		String description = descriptionField.value();
		create(ModelHelper.proposeName(), title, description);
	}

	private void create(String name, String title, String description) {
		LanguageRelease release = language.lastRelease();
		Model model = box().commands(ModelCommands.class).create(name, title, description, GavCoordinates.from(language, release), DisplayHelper.user(session()), username());
		createListener.accept(model);
	}

	private boolean check() {
		return DisplayHelper.check(titleField, this::translate) &&
			   DisplayHelper.check(descriptionField, this::translate);
	}

}