package io.quassar.editor.box.commands.language;

import io.intino.alexandria.logger.Logger;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Language;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class SaveLanguageReadmeCommand extends Command<Boolean> {
	public Language language;
	public String content;

	public SaveLanguageReadmeCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		try {
			File readmeFile = box.archetype().languages().readme(language.name());
			if (content == null) return false;
			Files.writeString(readmeFile.toPath(), content);
			return true;
		} catch (IOException e) {
			Logger.error(e);
			return false;
		}
	}

}
