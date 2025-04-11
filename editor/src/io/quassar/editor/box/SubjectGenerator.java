package io.quassar.editor.box;

import io.intino.alexandria.logger.Logger;
import io.quassar.editor.model.GavCoordinates;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

public class SubjectGenerator {
	private final EditorBox box;

	public SubjectGenerator(EditorBox box) {
		this.box = box;
	}

	public void generate() {
		Logger.info("Generating default subjects");
		registerModels();
		registerLanguages();
		Logger.info("Default subjects GENERATED!");
	}

	private void registerLanguages() {
		linesOf(box.archetype().configuration().defaultLanguages()).stream().skip(1).forEach(this::registerLanguage);
	}

	private void registerLanguage(String line) {
		String[] content = line.split("\t");
		Model metamodel = !content[2].isEmpty() ? box.modelManager().get(content[2]) : null;
		Language language = box.languageManager().create(content[0], content[1], metamodel, Language.Level.valueOf(content[3]), content[4], content[5]);
		box.languageManager().createRelease(language, content[6], null);
	}

	private void registerModels() {
		linesOf(box.archetype().configuration().defaultModels()).stream().skip(1).forEach(this::registerModel);
	}

	private void registerModel(String line) {
		String[] content = line.split("\t");
		box.modelManager().create(content[0], content[1], content[3], content[4], GavCoordinates.fromString(content[2]), false, null);
	}

	private List<String> linesOf(File file) {
		try {
			return Files.readAllLines(file.toPath());
		} catch (IOException e) {
			Logger.error(e);
			return Collections.emptyList();
		}
	}

}
