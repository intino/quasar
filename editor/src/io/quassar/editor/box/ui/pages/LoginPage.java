package io.quassar.editor.box.ui.pages;

import io.intino.alexandria.exceptions.*;
import java.time.*;
import java.util.*;
import io.quassar.editor.box.ui.displays.templates.*;

public class LoginPage extends AbstractLoginPage {

	public io.intino.alexandria.ui.Soul prepareSoul(io.intino.alexandria.ui.services.push.UIClient client) {
		return new io.intino.alexandria.ui.Soul(session) {
			@Override
			public void personify() {
				LoginTemplate component = new LoginTemplate(box);
				register(component);
				component.init();
			}
		};
	}
}