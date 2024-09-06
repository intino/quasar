package io.intino.ime.box.ui.pages;

import io.intino.ime.box.ui.displays.templates.PermissionsTemplate;

public class PermissionsPage extends AbstractPermissionsPage {
	public String model;

	public io.intino.alexandria.ui.Soul prepareSoul(io.intino.alexandria.ui.services.push.UIClient client) {
		return new io.intino.alexandria.ui.Soul(session) {
			@Override
			public void personify() {
				PermissionsTemplate component = new PermissionsTemplate(box);
				component.model(model);
				register(component);
				component.init();
				component.refresh();
			}
		};
	}
}