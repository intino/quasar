package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.services.push.User;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

public class HeaderTemplate extends AbstractHeaderTemplate<EditorBox> {
	private Language language;
	private String release;
	private Model model;

	public HeaderTemplate(EditorBox box) {
		super(box);
	}

	public void language(Language value) {
		this.language = value;
	}

	public void release(String release) {
		this.release = release;
	}

	public void model(Model value) {
		this.model = value;
	}

	@Override
	public void init() {
		super.init();
		login.onExecute(e -> {
			session().add("callback", session().browser().requestUrl());
			if (box().authService() == null) notifier.redirect(PathHelper.loginUrl(session()));
			else notifier.redirect(session().login(session().browser().baseUrl()));
		});
		user.onRefresh(e -> refreshUser());
	}

	@Override
	public void refresh() {
		super.refresh();
		User loggedUser = session().user();
		login.visible(loggedUser == null);
		user.visible(loggedUser != null);
	}

	private void refreshUser() {
		userLinksSelector.hide(myProjectsOption);
	}

}