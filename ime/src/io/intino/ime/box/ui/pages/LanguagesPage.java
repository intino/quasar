package io.intino.ime.box.ui.pages;

import io.intino.ime.box.ui.displays.templates.LanguagesTemplate;

public class LanguagesPage extends AbstractLanguagesPage {
	public String filters;

	public io.intino.alexandria.ui.Soul prepareSoul(io.intino.alexandria.ui.services.push.UIClient client) {
		return new io.intino.alexandria.ui.Soul(session) {
			@Override
			public void personify() {
				LanguagesTemplate component = new LanguagesTemplate(box);
				component.filters(filters);
				register(component);
				component.init();
				component.refresh();
			}
		};
	}
}