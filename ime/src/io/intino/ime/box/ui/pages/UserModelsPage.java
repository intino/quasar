package io.intino.ime.box.ui.pages;

import io.intino.ime.box.ui.displays.templates.UserHomeTemplate;

public class UserModelsPage extends AbstractUserModelsPage {
	public String user;

	public io.intino.alexandria.ui.Soul prepareSoul(io.intino.alexandria.ui.services.push.UIClient client) {
		return new io.intino.alexandria.ui.Soul(session) {
			@Override
			public void personify() {
				UserHomeTemplate component = new UserHomeTemplate(box);
				register(component);
				component.init();
			}
		};
	}
}