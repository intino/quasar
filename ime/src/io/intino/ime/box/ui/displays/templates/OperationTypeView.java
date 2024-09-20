package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.components.selector.SelectorOption;
import io.intino.ime.box.ImeBox;
import io.intino.ime.model.Operation;

import java.util.UUID;

public class OperationTypeView extends AbstractOperationTypeView<ImeBox> implements SelectorOption {
	private Operation.Type type;

	public OperationTypeView(ImeBox box) {
		super(box);
		id(UUID.randomUUID().toString());
	}

	public SelectorOption type(Operation.Type type) {
		this.type = type;
		name(type.name());
		return this;
	}

	@Override
	public String label() {
		return type.name();
	}

	@Override
	public void refresh() {
		super.refresh();
		nameField.value(type.name());
		iconField.icon(type.icon());
	}
}