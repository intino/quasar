package io.quassar.editor.box.commands.model;

import io.intino.alexandria.logger.Logger;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class SaveModelCommand extends Command<Boolean> {
	public Model model;
	public String name;
	public String title;
	public String description;

	public SaveModelCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		if (!model.name().equals(name) && !renameModel()) return false;
		model.name(name);
		model.title(title);
		model.description(description);
		box.modelManager().save(model);
		return true;
	}

	private boolean renameModel() {
		try {
			File currentFolder = box.archetype().languages().model(Language.nameOf(model.language()), model.name());
			File newFolder = box.archetype().languages().model(Language.nameOf(model.language()), name);
			Files.move(currentFolder.toPath(), newFolder.toPath());
			renameLanguage();
			return true;
		} catch (IOException e) {
			Logger.error(e);
			return false;
		}
	}

	private void renameLanguage() throws IOException {
		Language currentLanguage = box.languageManager().get(model.name());
		if (currentLanguage == null) return;
		File currentFolder = box.archetype().languages().language(model.name());
		File newFolder = box.archetype().languages().language(name);
		Files.move(currentFolder.toPath(), newFolder.toPath());
	}

}
