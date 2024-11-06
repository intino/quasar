package io.intino.ime.box.commands.language;

import io.intino.alexandria.Resource;
import io.intino.alexandria.logger.Logger;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.box.orchestator.ProjectCreator;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;
import io.intino.ime.model.Release;
import io.intino.ls.document.FileDocumentManager;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static io.intino.ime.box.scaffolds.ScaffoldFactory.Scaffold.Intellij;

public class CreateLanguageCommand extends Command<Language> {
	public String name;
	public Release parent;
	public String description;
	public Resource logo;
	public boolean isPrivate;

	public CreateLanguageCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Language execute() {
		Language language = box.languageManager().create(name, description, logo, parent, author(), isPrivate);
		Model model = box.modelManager().create(ModelHelper.proposeName(), name, parent, author(), name, isPrivate);
//		createWorkspace(model);
		return language;
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
