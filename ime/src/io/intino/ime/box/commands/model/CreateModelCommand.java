package io.intino.ime.box.commands.model;

import io.intino.alexandria.logger.Logger;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.box.orchestator.ProjectCreator;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;
import io.intino.ime.model.Release;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static io.intino.ime.box.scaffolds.ScaffoldFactory.Scaffold.Intellij;

public class CreateModelCommand extends Command<Model> {
	public String id;
	public String label;
	public Release release;
	public String owner;

	public CreateModelCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Model execute() {
		Model model = create();
		updateLanguage(model);
		createWorkspace(model);
		return model;
	}

	private Model create() {
		return box.modelManager().create(id, label, release, owner, null, author != null);
	}

	private void updateLanguage(Model model) {
		Language language = box.languageManager().get(model.modelingLanguage());
		language.modelsCount(language.modelsCount()+1);
		box.languageManager().save(language);
	}

	private void createWorkspace(Model model) {
		try {
			new ProjectCreator(model.id(), model.modelingLanguage(), "io.tafat",
					List.of(new ProjectCreator.CodeBucket("code/java/tafat", Intellij, box.configuration().defaultBuilder())))
					.create(ModelHelper.documentManager(model, author, box));
		} catch (GitAPIException | IOException | URISyntaxException e) {
			Logger.error(e);
		}
	}

}
