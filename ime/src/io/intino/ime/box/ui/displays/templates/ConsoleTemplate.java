package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.exceptions.*;
import io.intino.builderservice.schemas.Message;
import io.intino.ime.box.*;
import io.intino.ime.box.schemas.*;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.displays.templates.AbstractConsoleTemplate;

import java.util.List;

public class ConsoleTemplate extends AbstractConsoleTemplate<ImeBox> {

	public ConsoleTemplate(ImeBox box) {
		super(box);
	}

	public void messages(List<Message> messages) {
	}
}