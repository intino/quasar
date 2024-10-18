package io.intino.ime.box.ui.displays.templates;

import io.intino.builderservice.schemas.BuilderInfo;
import io.intino.ime.box.ImeBox;

public class BuilderPropertyView extends AbstractBuilderPropertyView<ImeBox> {
	private BuilderInfo.Properties property;

	public BuilderPropertyView(ImeBox box) {
		super(box);
	}

	public void property(BuilderInfo.Properties property) {
		this.property = property;
	}

	@Override
	public void refresh() {
		super.refresh();
		name.value(property.name());
		value.value(property.value());
	}
}