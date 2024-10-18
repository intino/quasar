package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.exceptions.*;
import io.intino.ime.box.*;
import io.intino.ime.box.schemas.*;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.displays.templates.AbstractTagEditor;

import java.util.function.Consumer;

public class TagEditor extends AbstractTagEditor<ImeBox> {
	private String tag;
	private Consumer<String> removeListener;

	public TagEditor(ImeBox box) {
		super(box);
	}

	public void tag(String tag) {
		this.tag = tag;
	}

	public void onRemove(Consumer<String> listener) {
		this.removeListener = listener;
	}

	@Override
	public void init() {
		super.init();
		delete.onExecute(e -> removeListener.accept(tag));
	}

	@Override
	public void refresh() {
		super.refresh();
		nameField.value(tag);
	}
}