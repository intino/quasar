package io.intino.ime.box.commands.model;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.model.Model;
import io.intino.ime.model.Operation;

public class ExecuteModelOperationCommand extends Command<Boolean> {
	public Model model;
	public Operation operation;

	public ExecuteModelOperationCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		return true;
	}

}
