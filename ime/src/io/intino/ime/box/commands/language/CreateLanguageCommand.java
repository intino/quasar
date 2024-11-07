package io.intino.ime.box.commands.language;

import io.intino.alexandria.Resource;
import io.intino.alexandria.exceptions.Conflict;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.alexandria.logger.Logger;
import io.intino.builderservice.schemas.BuilderInfo;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.box.scaffolds.ScaffoldFactory;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;
import io.intino.ime.model.Operation;
import io.intino.ime.model.Release;

import java.util.Optional;

public class CreateLanguageCommand extends Command<Language> {
	public String name;
	public Release parent;
	public String group;
	public ScaffoldFactory.Language programmingLanguage;
	public ScaffoldFactory.Scaffold scaffold;
	public String description;
	public Resource logo;
	public boolean isPrivate;

	public CreateLanguageCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Language execute() {
		Language language = box.languageManager().create(name, group, description, logo, parent, author(), isPrivate);
		save(builderInfo(), language);
		Model model = box.modelManager().create(ModelHelper.proposeName(), name, parent, author(), name, isPrivate);
		ModelHelper.createProject(model, language, programmingLanguage, scaffold, author, box);
		return language;
	}

	private void save(BuilderInfo info, Language language) {
		if (info == null) return;
		language.operations(info.operations().stream().map(Operation::new).toList());
		language.programmingLanguages(info.targetLanguages());
		box.languageManager().save(language);
	}

	private BuilderInfo builderInfo() {
		try {
			return box.builderService().getBuilderInfo(box.configuration().defaultBuilder(), box.tokenProvider().of(author).dockerHubToken());
		} catch (Conflict | InternalServerError e) {
			Logger.error(e);
			return null;
		}
	}

}
