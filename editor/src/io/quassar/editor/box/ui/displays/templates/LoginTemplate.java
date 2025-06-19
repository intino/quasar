package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.Display;
import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.displays.components.selector.SelectorOption;
import io.intino.alexandria.ui.displays.events.Event;
import io.intino.alexandria.ui.displays.events.KeyPressEvent;
import io.intino.alexandria.ui.displays.events.SelectionEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.UserCommands;
import io.quassar.editor.box.ui.displays.GoogleLoginDisplay;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.box.util.UrlHelper;
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
		homeLink.site(PathHelper.homeUrl(session()));
		refreshGoogleLoginBlock();
		refreshLocalLoginBlock();
	}

	private void refreshGoogleLoginBlock() {
		googleLoginBlock.visible(!box().configuration().googleClientId().isEmpty());
		if (!googleLoginBlock.isVisible()) return;
		if (googleLoginStamp.display() == null) createLoginDisplay();
		googleLoginStamp.refresh();
	}

	private void createLoginDisplay() {
		GoogleLoginDisplay display = new GoogleLoginDisplay(box());
		display.onSuccess(this::login);
		display.onFailure(e -> notifyUser(translate("Could not login using Google"), UserMessage.Type.Error));
		googleLoginStamp.display(display);
	}

	private void refreshLocalLoginBlock() {
		localLoginBlock.visible(UrlHelper.isLocalUrl(session().browser().requestUrl()));
		if (!localLoginBlock.isVisible()) return;
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
		if (selectedUser == null || selectedUser.isEmpty()) {
			notFoundUsernameMessage.visible(true);
			return;
		}
		notFoundUsernameMessage.visible(false);
		login(selectedUser);
	}

	private void updateSelectedUser(SelectionEvent event) {
		String selected = !event.selection().isEmpty() ? (String) event.selection().getFirst() : null;
		LoginSelectorOption display = userSelector.children().stream().filter(d -> d instanceof LoginSelectorOption && d.name().equals(selected)).map(d -> (LoginSelectorOption)d).findFirst().orElse(null);
		selectedUser = display != null ? display.username() : null;
	}

	private void updateSelectedUser(KeyPressEvent event) {
		selectedUser = usernameField.value();
		if (selectedUser == null || selectedUser.isEmpty()) {
			notFoundUsernameMessage.visible(true);
			return;
		}
		notFoundUsernameMessage.visible(false);
		login(selectedUser);
	}

	private void login(String username) {
		login(new io.intino.alexandria.ui.services.push.User().username(username).fullName(username).email(username));
	}

	private void login(io.intino.alexandria.ui.services.push.User userInfo) {
		User user = box().userManager().get(userInfo.email());
		if (user == null) user = box().commands(UserCommands.class).create(userInfo.email(), username());
		session().user(userInfo);
		notifier.redirect(redirectUrl(user));
	}

	private String redirectUrl(User user) {
		String url = session().preference("callback");
		if (url != null && !url.isEmpty()) return url;
		return PathHelper.homeUrl(session());
	}


}