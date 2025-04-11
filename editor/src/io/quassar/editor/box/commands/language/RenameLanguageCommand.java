package io.quassar.editor.box.commands.language;

import io.intino.alexandria.logger.Logger;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Language;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class RenameLanguageCommand extends Command<Boolean> {
	public Language language;
	public String newName;

	public RenameLanguageCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		if (language.name().equals(newName)) return true;
		return renameLanguage();
	}

	private boolean renameLanguage() {
		try {
			File currentFolder = box.archetype().languages().get(language.id());
			File newFolder = box.archetype().languages().get(Language.id(language.group(), newName));
			Files.move(currentFolder.toPath(), newFolder.toPath());
			language.name(newName);
			return true;
		} catch (IOException e) {
			Logger.error(e);
			return false;
		}
	}

}
