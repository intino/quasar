package io.quassar.editor.box.ui.pages;

import io.quassar.editor.box.ui.displays.templates.HomeTemplate;
import io.quassar.editor.box.util.PathHelper;

public class LanguagePage extends AbstractLanguagePage {
	public String group;
	public String language;
	public String version;
	public String tab;
	public String view;

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