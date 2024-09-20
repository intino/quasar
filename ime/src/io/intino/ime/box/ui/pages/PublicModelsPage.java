package io.intino.ime.box.ui.pages;

import io.intino.ime.box.ui.displays.templates.LanguageModelsTemplate;

public class PublicModelsPage extends AbstractPublicModelsPage {
	public String language;

	public io.intino.alexandria.ui.Soul prepareSoul(io.intino.alexandria.ui.services.push.UIClient client) {
		return new io.intino.alexandria.ui.Soul(session) {
			@Override
			public void personify() {
				LanguageModelsTemplate component = new LanguageModelsTemplate(box);
				component.language(language);
				register(component);
				component.init();
				component.refresh();
			}
		};
	}
}