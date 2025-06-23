package io.quassar.editor.box.commands.language;

import io.intino.alexandria.logger.Logger;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.GavCoordinates;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class RenameLanguageCommand extends Command<Language> {
	public Language language;
	public String newId;

	public RenameLanguageCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Language execute() {
		String oldKey = language.key();
		if (box.languageManager().exists(Language.collectionFrom(newId), Language.nameFrom(newId))) return language;
		if (oldKey.equalsIgnoreCase(newId)) return language;
		boolean renamed = renameLanguage();
		if (renamed) updateModelsWithLanguage(oldKey);
		return box.languageManager().get(newId);
	}

	private boolean renameLanguage() {
		try {
			File currentFolder = box.archetype().languages().get(language.key());
			File newFolder = box.archetype().languages().get(newId());
			Files.move(currentFolder.toPath(), newFolder.toPath());
			box.languageManager().rename(language, newId());
			return true;
		} catch (IOException e) {
			Logger.error(e);
			return false;
		}
	}

	private void updateModelsWithLanguage(String oldKey) {
		List<Model> models = box.modelManager().models(Language.collectionFrom(oldKey), Language.nameFrom(oldKey));
		models.forEach(m -> m.language(new GavCoordinates(Language.collectionFrom(newId()), language.name(), m.language().version())));
	}

	private String newId() {
		return newId.toLowerCase();
	}

}
