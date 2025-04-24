package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.Display;
import io.intino.alexandria.ui.displays.components.selector.SelectorOption;
import io.intino.alexandria.ui.displays.events.Event;
import io.intino.alexandria.ui.displays.events.KeyPressEvent;
import io.intino.alexandria.ui.displays.events.SelectionEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.UserCommands;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.model.User;

public class LoginTemplate extends AbstractLoginTemplate<EditorBox> {
	private String selectedUser;

	public LoginTemplate(EditorBox box) {
		super(box);
	}

	@Override
	public void init() {
		super.init();
		initLoginUsingUsername();
		refresh();
	}

	@Override
	public void refresh() {
		super.refresh();
		refreshUsernameLoginBlock();
	}

	private void refreshUsernameLoginBlock() {
		userSelector.clear();
		box().userManager().users().forEach(u -> userSelector.add((SelectorOption) optionDisplayFor(u)));
		userSelector.children().stream().filter(d -> d instanceof LoginSelectorOption).forEach(Display::refresh);
	}

	private LoginSelectorOption optionDisplayFor(User user) {
		return new LoginSelectorOption(box()).user(user).onRemove(e -> removeUser(user));
	}

	private void removeUser(User user) {
		box().userManager().remove(user);
		refresh();
	}

	private void initLoginUsingUsername() {
		userSelector.onSelect(this::updateSelectedUser);
		usernameField.onEnterPress(this::updateSelectedUser);
		login.onExecute(this::loginUsingUsername);
	}

	private void loginUsingUsername(Event event) {
		if (selectedUser == null) selectedUser = usernameField.value();
		login(selectedUser);
	}

	private void updateSelectedUser(SelectionEvent event) {
		String selected = !event.selection().isEmpty() ? (String) event.selection().getFirst() : null;
		LoginSelectorOption display = userSelector.children().stream().filter(d -> d instanceof LoginSelectorOption && d.name().equals(selected)).map(d -> (LoginSelectorOption)d).findFirst().orElse(null);
		selectedUser = display != null ? display.username() : null;
	}

	private void updateSelectedUser(KeyPressEvent event) {
		selectedUser = usernameField.value();
		login(selectedUser);
	}

	private void login(String username) {
		User user = box().userManager().get(username);
		if (user == null) user = box().commands(UserCommands.class).create(username, username());
		session().user(new io.intino.alexandria.ui.services.push.User().username(user.name()).fullName(user.name()));
		notifier.redirect(redirectUrl(user));
	}

	private String redirectUrl(User user) {
		String url = session().preference("callback");
		if (url != null && !url.isEmpty()) return url;
		return PathHelper.homeUrl(session());
	}

}