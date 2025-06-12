package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.model.User;

import java.util.function.Consumer;

public class CollaboratorItemTemplate extends AbstractCollaboratorItemTemplate<EditorBox> {
	private String user;
	private Consumer<String> removeListener;

	public CollaboratorItemTemplate(EditorBox box) {
		super(box);
	}

	public void user(String user) {
		this.user = user;
	}

	public void onRemove(Consumer<String> listener) {
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
		name.value(user);
	}

}