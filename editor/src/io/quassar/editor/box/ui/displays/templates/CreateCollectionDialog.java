package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.CollectionCommands;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.model.Collection;

import java.util.function.Consumer;

public class CreateCollectionDialog extends AbstractCreateCollectionDialog<EditorBox> {
	private Consumer<Collection> createListener;

	public CreateCollectionDialog(EditorBox box) {
		super(box);
	}

	public void onCreate(Consumer<Collection> listener) {
		this.createListener = listener;
	}

	@Override
	public void init() {
		super.init();
		nameField.onEnterPress(e -> create());
		nameField.onChange(e -> refreshState());
		create.onExecute(e -> create());
		dialog.onOpen(e -> refreshDialog());
	}

	private void refreshDialog() {
		nameField.error(null);
		nameField.value(null);
		nameField.focus();
		validIcon.visible(false);
		invalidIcon.visible(false);
	}

	private void refreshState() {
		DisplayHelper.CheckResult result = DisplayHelper.checkCollectionName(nameField.value(), this::translate, box());
		nameField.error(!result.success() ? result.message() : null);
		validIcon.visible(result.success());
		invalidIcon.visible(!result.success());
	}

	private void create() {
		nameField.error(null);
		DisplayHelper.CheckResult result = DisplayHelper.checkCollectionName(nameField.value(), this::translate, box());
		if (!result.success()) {
			nameField.error(result.message());
			return;
		}
		dialog.close();
		io.quassar.editor.model.Collection collection = box().commands(CollectionCommands.class).create(nameField.value(), username());
		createListener.accept(collection);
	}

}