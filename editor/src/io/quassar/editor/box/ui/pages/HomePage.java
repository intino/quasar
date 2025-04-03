package io.quassar.editor.box.ui.pages;

import io.quassar.editor.box.ui.displays.templates.HomeTemplate;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.box.util.PermissionsHelper;

public class HomePage extends AbstractHomePage {
	public String language;
	public String view;
	public String dialog;

	@Override
	public String redirectUrl() {
		session.add("callback", session.browser().requestUrl());
		return PathHelper.loginUrl(session);
	}

	public io.intino.alexandria.ui.Soul prepareSoul(io.intino.alexandria.ui.services.push.UIClient client) {
		session.language("en");
		return new io.intino.alexandria.ui.Soul(session) {
			@Override
			public void personify() {
				HomeTemplate component = new HomeTemplate(box);
				register(component);
				component.init();
			}
		};
	}
}