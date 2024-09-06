package io.intino.ime.box.ui.pages;

import io.intino.ime.box.ui.displays.templates.ModelsTemplate;

public class ModelsPage extends AbstractModelsPage {
	public String user;

	public io.intino.alexandria.ui.Soul prepareSoul(io.intino.alexandria.ui.services.push.UIClient client) {
		return new io.intino.alexandria.ui.Soul(session) {
			@Override
			public void personify() {
				ModelsTemplate component = new ModelsTemplate(box);
				register(component);
				component.init();
			}
		};
	}
}