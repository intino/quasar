package io.quassar.editor.box.ui.pages;

import io.intino.alexandria.exceptions.*;
import java.time.*;
import java.util.*;
import io.quassar.editor.box.ui.displays.templates.*;

public class NotFoundPage extends AbstractNotFoundPage {
	public String type;

	public io.intino.alexandria.ui.Soul prepareSoul(io.intino.alexandria.ui.services.push.UIClient client) {
		return new io.intino.alexandria.ui.Soul(session) {
			@Override
			public void personify() {
				NotFoundTemplate component = new NotFoundTemplate(box);
				component.type(type);
				register(component);
				component.init();
				component.refresh();
			}
		};
	}
}