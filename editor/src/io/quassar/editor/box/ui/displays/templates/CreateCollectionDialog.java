package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.CollectionCommands;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.model.Collection;

import java.util.function.Consumer;

public class CreateCollectionDialog extends AbstractCreateCollectionDialog<EditorBox> {
	private Consumer<Collection> createListener;
	private Mode mode = Mode.Normal;

	public CreateCollectionDialog(EditorBox box) {
		super(box);
	}

	public void onCreate(Consumer<Collection> listener) {
		this.createListener = listener;
	}

	public enum Mode { Normal, FirstTime }
	public void mode(Mode mode) {
		this.mode = mode;
	}

	public void open() {
		dialog.open();
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
		dialog.title(translate(mode == Mode.FirstTime ? "Create your first collection" : "Create collection"));
		hint.value(translate(mode == Mode.FirstTime ? "You don't have any collections yet. Create one now to start building DSLs." : "Collections group related DSLs by project or team. Enter a name to create another one"));
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
		io.quassar.editor.model.Collection collection = box().commands(CollectionCommands.class).create(nameField.value(), Collection.SubscriptionPlan.Professional, username());
		createListener.accept(collection);
	}

}