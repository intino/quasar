package io.intino.ime.box.commands.language;

import io.intino.alexandria.Resource;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.alexandria.logger.Logger;
import io.intino.builderservice.schemas.BuilderInfo;
import io.intino.builderservice.schemas.RegisterBuilder;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.Command;
import io.intino.ime.model.Language;
import io.intino.ime.model.Operation;

import java.util.List;

public class SaveLanguageCommand extends Command<Boolean> {
	public Language language;
	public String group;
	public String description;
	public boolean isPrivate;
	public String builder;
	public Resource logo;
	public List<String> programmingLanguages;
	public List<Operation> operations;
	public List<String> tags;

	public SaveLanguageCommand(ImeBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		saveLanguage();
		registerBuilder();
		return true;
	}

	private void registerBuilder() {
		try {
			box.builderService().postBuilders(builderInfo());
		} catch (InternalServerError e) {
			Logger.error(e);
		}
	}

	private RegisterBuilder builderInfo() {
		RegisterBuilder result = new RegisterBuilder();
		result.imageURL(builder);
		result.registryToken(box.tokenProvider().of(author).dockerHubToken());
		return result;
	}

	private void saveLanguage() {
		language.group(group);
		language.description(description);
		language.isPrivate(isPrivate);
		language.builder(builder);
		language.programmingLanguages(programmingLanguages);
		language.operations(operations);
		language.tags(tags);
		box.languageManager().save(language);
		if (logo != null) box.languageManager().saveLogo(language, logo);
	}

}
