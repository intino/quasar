package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.components.selector.SelectorOption;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.model.User;

import java.util.UUID;
import java.util.function.Consumer;

public class LoginSelectorOption extends AbstractLoginSelectorOption<EditorBox> implements SelectorOption {
	private User user;
	private Consumer<User> removeListener;

	public LoginSelectorOption(EditorBox box) {
		super(box);
		id(UUID.randomUUID().toString());
		name(UUID.randomUUID().toString());
	}

	public String username() {
		return user.name();
	}

	public LoginSelectorOption user(User user) {
		this.user = user;
		return this;
	}

	public LoginSelectorOption onRemove(Consumer<User> listener) {
		this.removeListener = listener;
		return this;
	}

	@Override
	public void update() {
		super.update();
		refresh();
	}

	@Override
	public void init() {
		super.init();
		removeUser.onExecute(e -> removeListener.accept(user));
	}

	@Override
	public void refresh() {
		super.refresh();
		username.value(user.name());
	}
}