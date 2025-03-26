package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.model.User;

import java.util.function.Consumer;

public class CollaboratorItemTemplate extends AbstractCollaboratorItemTemplate<EditorBox> {
	private User user;
	private Consumer<User> removeListener;

	public CollaboratorItemTemplate(EditorBox box) {
		super(box);
	}

	public void user(User user) {
		this.user = user;
	}

	public void onRemove(Consumer<User> listener) {
		this.removeListener = listener;
	}

	@Override
	public void init() {
		super.init();
		removeUser.onExecute(e -> removeListener.accept(user));
	}

	@Override
	public void refresh() {
		super.refresh();
		name.value(user.name());
	}

}