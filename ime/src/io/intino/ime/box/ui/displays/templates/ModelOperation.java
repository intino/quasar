package io.intino.ime.box.ui.displays.templates;

import io.intino.ime.box.ImeBox;
import io.intino.ime.model.Operation;

import java.util.function.Consumer;

public class ModelOperation extends AbstractModelOperation<ImeBox> {
	private Operation operation;
	private Consumer<Operation> executeListener;

	public ModelOperation(ImeBox box) {
		super(box);
	}

	public void operation(Operation operation) {
		this.operation = operation;
	}

	public void onExecute(Consumer<Operation> listener) {
		this.executeListener = listener;
	}

	@Override
	public void init() {
		super.init();
		trigger.onExecute(e -> executeListener.accept(operation));
	}

	@Override
	public void refresh() {
		super.refresh();
		trigger.title(operation.name());
		trigger.icon(operation.type().icon());
	}
}