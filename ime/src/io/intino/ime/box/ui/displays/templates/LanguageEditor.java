package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.Resource;
import io.intino.alexandria.ui.displays.events.actionable.ToggleEvent;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.model.Language;

import java.net.URL;
import java.util.function.Consumer;

public class LanguageEditor extends AbstractLanguageEditor<ImeBox> {
	private Language parent;
	private Resource logo = null;
	private Consumer<Boolean> acceptListener;

	public LanguageEditor(ImeBox box) {
		super(box);
	}

	public void parent(Language value) {
		this.parent = value;
	}

	public String name() {
		return nameField.value();
	}

	public String description() {
		return descriptionField.value();
	}

	public Resource logo() {
		return logo;
	}

	public boolean isPrivate() {
		return accessTypeField.state() == ToggleEvent.State.On;
	}

	public void onAccept(Consumer<Boolean> listener) {
		this.acceptListener = listener;
	}

	public boolean check() {
		if (!DisplayHelper.checkLanguageName(nameField, this::translate, box())) return false;
		return DisplayHelper.check(descriptionField, this::translate);
	}

	public void reset() {
		nameField.value(null);
		descriptionField.value(null);
		logoField.value((URL)null);
		accessTypeField.state(ToggleEvent.State.On);
	}

	@Override
	public void init() {
		super.init();
		logoField.onChange(e -> logo = e.value());
		descriptionField.onEnterPress(e -> acceptListener.accept(true));
	}
}