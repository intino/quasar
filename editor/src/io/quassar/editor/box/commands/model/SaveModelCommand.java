package io.quassar.editor.box.commands.model;

import io.intino.alexandria.logger.Logger;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.commands.language.DeployLanguageCommand;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class SaveModelCommand extends Command<Boolean> {
	public Model model;
	public String name;
	public String title;
	public String hint;
	public String description;

	public SaveModelCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		if (!model.name().equals(name) && !renameModel()) return false;
		else saveModel();
		return true;
	}

	private boolean renameModel() {
		try {
			File currentFolder = box.archetype().languages().model(Language.nameOf(model.language()), model.name());
			File newFolder = box.archetype().languages().model(Language.nameOf(model.language()), name);
			Files.move(currentFolder.toPath(), newFolder.toPath());
			Language language = box.languageManager().get(model.name());
			if (language == null) return true;
			String oldName = model.name();
			saveModel();
			renameLanguage(language, oldName);
			createRelease(language);
			publishLanguage(language);
			return true;
		} catch (IOException e) {
			Logger.error(e);
			return false;
		}
	}

	private void saveModel() {
		model.name(name);
		model.title(title);
		model.hint(hint);
		model.description(description);
		box.modelManager().save(model);
	}

	private void createRelease(Language language) {
		CreateModelReleaseCommand command = new CreateModelReleaseCommand(box);
		command.author = author;
		command.model = model;
		command.version = language.version();
		command.execute();
	}

	private void renameLanguage(Language language, String oldName) throws IOException {
		File currentFolder = box.archetype().languages().language(oldName);
		File newFolder = box.archetype().languages().language(name);
		Files.move(currentFolder.toPath(), newFolder.toPath());
		language.name(name);
		box.languageManager().save(language);
	}

	private void publishLanguage(Language language) {
		DeployLanguageCommand command = new DeployLanguageCommand(box);
		command.author = author;
		command.name = language.name();
		command.version = language.version();
		command.level = language.level();
		command.execute();
	}

}
