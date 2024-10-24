package io.intino.ime.box.ui.displays.templates;

import io.intino.ime.box.ImeBox;
import io.intino.ime.model.Operation;

import java.util.Arrays;
import java.util.function.Consumer;

public class OperationEditor extends AbstractOperationEditor<ImeBox> {
	private Operation operation;
	private Consumer<Operation> changeListener;

	public OperationEditor(ImeBox box) {
		super(box);
	}

	public void operation(Operation operation) {
		this.operation = operation;
	}

	public Operation operation() {
		return new Operation(nameField.value(), Operation.Type.valueOf(typeField.selection().getFirst()));
	}

	public void onChange(Consumer<Operation> listener) {
		this.changeListener = listener;
	}

	@Override
	public void init() {
		super.init();
		typeField.onSelect(e -> {
			operation.type(Operation.Type.valueOf((String) e.selection().getFirst()));
			notifyChange();
		});
	}

	@Override
	public void refresh() {
		super.refresh();
		nameField.value(operation.name());
		typeField.readonly(false);
		refreshIcons();
	}

	private void refreshIcons() {
		typeField.clear();
		Arrays.stream(Operation.Type.values()).forEach(t -> typeField.add(new OperationTypeView(box()).type(t)));
		if (operation.type() != null) typeField.selection(operation.type().name());
	}

	private void notifyChange() {
		changeListener.accept(operation);
	}

}