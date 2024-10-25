package io.intino.ime.box.commands.model;

import io.intino.alexandria.logger.Logger;
import io.intino.builderservice.schemas.Message;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.box.orchestator.BuilderOrchestator;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;
import io.intino.ime.model.Operation;
import io.intino.ls.document.FileDocumentManager;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;

public class ExecuteModelOperationCommand extends Command<List<Message>> {
	public Model model;
	public Operation operation;

	public ExecuteModelOperationCommand(ImeBox box) {
		super(box);
	}

	@Override
	public List<Message> execute() {
		try {
			Language language = box.languageManager().get(model.modelingLanguage());
			URL builderServiceUrl = URI.create(box.configuration().builderServiceUrl()).toURL();
			FileDocumentManager documentManager = new FileDocumentManager(new File(box.modelManager().workspace(model)));
			BuilderOrchestator orchestator = new BuilderOrchestator(builderServiceUrl, documentManager);
			return orchestator.exec(author, language.builder(), operation.name());
		} catch (IOException e) {
			Logger.error(e);
			return Collections.emptyList();
		}
	}

}
