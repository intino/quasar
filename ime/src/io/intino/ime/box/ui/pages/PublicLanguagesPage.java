package io.intino.ime.box.ui.pages;

import io.intino.ime.box.ui.displays.templates.*;

public class PublicLanguagesPage extends AbstractPublicLanguagesPage {
	public String language;

	public io.intino.alexandria.ui.Soul prepareSoul(io.intino.alexandria.ui.services.push.UIClient client) {
		return new io.intino.alexandria.ui.Soul(session) {
			@Override
			public void personify() {
				ParentLanguagesTemplate component = new ParentLanguagesTemplate(box);
				component.language(language);
				register(component);
				component.init();
				component.refresh();
			}
		};
	}
}