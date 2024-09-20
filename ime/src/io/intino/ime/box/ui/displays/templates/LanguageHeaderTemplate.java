package io.intino.ime.box.ui.displays.templates;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.model.Language;

public class LanguageHeaderTemplate extends AbstractLanguageHeaderTemplate<ImeBox> {
	private Language language;

	public LanguageHeaderTemplate(ImeBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	@Override
	public void init() {
		super.init();
		login.onExecute(e -> {
			session().add("callback", session().browser().requestUrl());
			notifier.redirect(session().login(session().browser().baseUrl()));
		});
	}

	@Override
	public void refresh() {
		super.refresh();
		notLoggedToolbar.visible(user() == null);
		login.visible(user() == null);
		user.visible(user() != null);
		if (session().user() == null) return;
		userHome.path(PathHelper.userHomePath(session()));
	}
}