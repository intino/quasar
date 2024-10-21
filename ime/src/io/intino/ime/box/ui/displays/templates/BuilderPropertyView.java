package io.intino.ime.box.ui.displays.templates;

import io.intino.ime.box.ImeBox;

public class BuilderPropertyView extends AbstractBuilderPropertyView<ImeBox> {
	private String _property;
	private String _value;

	public BuilderPropertyView(ImeBox box) {
		super(box);
	}

	public void property(String property, String value) {
		this._property = property;
		this._value = value;
	}

	@Override
	public void refresh() {
		super.refresh();
		name.value(_property);
		value.value(_value);
	}
}