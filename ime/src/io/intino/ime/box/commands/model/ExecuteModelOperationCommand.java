package io.intino.ime.box.commands.model;

import io.intino.alexandria.Resource;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.alexandria.logger.Logger;
import io.intino.builderservice.schemas.RunOperationContext;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.box.orchestator.BuilderOrchestator;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;
import io.intino.ime.model.Operation;

import java.io.IOException;
import java.io.InputStream;

public class ExecuteModelOperationCommand extends Command<String> {
	public Model model;
	public Operation operation;

	public ExecuteModelOperationCommand(ImeBox box) {
		super(box);
	}

	@Override
	public String execute() {
		//new BuilderOrchestator(box.configuration().builderServiceUrl(), box..workspace(model));
		return null;
	}

}
