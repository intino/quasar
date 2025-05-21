package io.quassar.editor.box.ui.pages;

import io.quassar.editor.box.ui.displays.templates.HomeTemplate;

public class AboutPage extends AbstractAboutPage {

	public io.intino.alexandria.ui.Soul prepareSoul(io.intino.alexandria.ui.services.push.UIClient client) {
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