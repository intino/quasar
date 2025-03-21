package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.services.push.User;
import io.quassar.editor.box.EditorBox;

public class HeaderTemplate extends AbstractHeaderTemplate<EditorBox> {

	public HeaderTemplate(EditorBox box) {
		super(box);
	}

	@Override
	public void refresh() {
		super.refresh();
		User loggedUser = session().user();
		login.visible(loggedUser == null);
		user.visible(loggedUser != null);
		notLoggedBlock.visible(loggedUser == null);
	}

}