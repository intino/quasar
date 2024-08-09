package io.intino.ime.box.ui.pages;

import io.intino.ime.box.ui.displays.templates.NotFoundTemplate;

public class NotFoundPage extends AbstractNotFoundPage {
	public String workspace;

	public io.intino.alexandria.ui.Soul prepareSoul(io.intino.alexandria.ui.services.push.UIClient client) {
		return new io.intino.alexandria.ui.Soul(session) {
			@Override
			public void personify() {
				NotFoundTemplate component = new NotFoundTemplate(box);
				component.workspace(workspace);
				register(component);
				component.init();
				component.refresh();
			}
		};
	}
}