package io.intino.ime.box.commands.language;

import io.intino.alexandria.Resource;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.box.orchestator.ProjectCreator;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;
import io.intino.ime.model.Release;
import io.intino.ls.document.FileDocumentManager;

import java.io.File;
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
		box.modelManager().create(ModelHelper.proposeName(), name, parent, author(), name, isPrivate);
//		new ProjectCreator("Tafat:1.0.0", parent.id(), "io.tafat",
//				List.of(new ProjectCreator.CodeBucket("code/java/tafat", Intellij, "io.intino.magritte.builder:1.3.0")))
//				.create(new FileDocumentManager(new File("temp/projects/tafat")));
		return language;
	}

}
