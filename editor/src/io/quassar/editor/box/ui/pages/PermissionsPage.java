package io.quassar.editor.box.ui.pages;

import io.quassar.editor.box.ui.displays.templates.PermissionsTemplate;

public class PermissionsPage extends AbstractPermissionsPage {
	public String username;
	public String language;
	public String model;
	public String callback;

	public io.intino.alexandria.ui.Soul prepareSoul(io.intino.alexandria.ui.services.push.UIClient client) {
		return new io.intino.alexandria.ui.Soul(session) {
			@Override
			public void personify() {
				PermissionsTemplate component = new PermissionsTemplate(box);
				component.language(language);
				component.model(model);
				component.callback(callback);
				register(component);
				component.init();
				component.refresh();
			}
		};
	}
}