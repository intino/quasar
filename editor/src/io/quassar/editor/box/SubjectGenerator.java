package io.quassar.editor.box;

import io.intino.alexandria.logger.Logger;
import io.quassar.editor.box.util.DirUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class SubjectGenerator {
	private final EditorBox box;

	public SubjectGenerator(EditorBox box) {
		this.box = box;
	}

	public void generate() {
		Logger.info("Generating default subjects");
		createIndex();
		DirUtils.copyDir("datamart/default-languages", box.archetype().languages().root());
		DirUtils.copyDir("datamart/default-models", box.archetype().models().root());
		Logger.info("Default subjects GENERATED!");
	}

	private void createIndex() {
		try (InputStream stream = SubjectGenerator.class.getResourceAsStream("/datamart/index.triples")) {
			if (stream == null) return;
			Files.copy(stream, box.archetype().index().toPath());
		} catch (IOException e) {
			Logger.error(e);
		}
	}

}
