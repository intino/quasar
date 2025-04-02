package io.quassar.editor.box.ui.pages;

import io.quassar.editor.box.ui.displays.templates.LoginTemplate;

public class LoginPage extends AbstractLoginPage {

	@Override
	public boolean hasPermissions() {
		return box.authService() == null;
	}

	public io.intino.alexandria.ui.Soul prepareSoul(io.intino.alexandria.ui.services.push.UIClient client) {
		session.language("en");
		return new io.intino.alexandria.ui.Soul(session) {
			@Override
			public void personify() {
				LoginTemplate component = new LoginTemplate(box);
				register(component);
				component.init();
			}
		};
	}
}