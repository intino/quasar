package io.intino.ime.box.ui.pages;

import io.intino.ime.box.ui.displays.templates.HomeTemplate;

public class SearchPage extends AbstractSearchPage {
	public String condition;

	public io.intino.alexandria.ui.Soul prepareSoul(io.intino.alexandria.ui.services.push.UIClient client) {
		return new io.intino.alexandria.ui.Soul(session) {
			@Override
			public void personify() {
				HomeTemplate component = new HomeTemplate(box);
				register(component);
				component.init();
				component.openSearch(condition);
			}
		};
	}
}