package io.quassar.editor.box;

import io.intino.alexandria.logger.Logger;
import io.quassar.editor.model.GavCoordinates;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;
import io.quassar.editor.model.User;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

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
		List<String> lines = linesOf(box.archetype().configuration().defaultLanguages(), "datamart/default-languages.tsv");
		lines.stream().skip(1).forEach(this::registerLanguage);
		copyDir("datamart/default-languages", box.archetype().languages().root());
	}

	private void registerLanguage(String line) {
		String[] content = line.split("\t");
		Model metamodel = !content[2].isEmpty() ? box.modelManager().get(content[2]) : null;
		Language language = box.languageManager().create(content[0], content[1], metamodel, Language.Level.valueOf(content[3]), content[4], content[5]);
		box.languageManager().createRelease(language, content[6], null);
	}

	private void registerModels() {
		List<String> lines = linesOf(box.archetype().configuration().defaultModels(), "datamart/default-models.tsv");
		lines.stream().skip(1).forEach(this::registerModel);
		copyDir("datamart/default-models", box.archetype().models().root());
	}

	private void copyDir(String dir, File destiny) {
		try {
			Path source = Paths.get(SubjectGenerator.class.getResource("/" + dir).toURI());
			try(Stream<Path> paths = Files.walk(source)) {
				paths.forEach(path -> {
					try {
						Path destinoPath = destiny.toPath().resolve(source.relativize(path).toString());
						if (Files.isDirectory(path)) {
							Files.createDirectories(destinoPath);
						} else {
							Files.createDirectories(destinoPath.getParent());
							Files.copy(path, destinoPath, StandardCopyOption.REPLACE_EXISTING);
						}
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			}
		} catch (URISyntaxException | IOException e) {
			Logger.error(e);
		}
	}

	private void registerModel(String line) {
		String[] content = line.split("\t");
		Model model = box.modelManager().create(content[0], content[1], content[3], content[4], GavCoordinates.fromString(content[2]), Model.Usage.EndUser, User.Quassar);
		model.isPrivate(false);
	}

	private List<String> linesOf(File file, String defaultResource) {
		return file.exists() ? linesOf(file) : linesOf(SubjectGenerator.class.getResource("/" + defaultResource));
	}

	private List<String> linesOf(File file) {
		try {
			return Files.readAllLines(file.toPath());
		} catch (IOException e) {
			Logger.error(e);
			return Collections.emptyList();
		}
	}

	private List<String> linesOf(URL resource) {
		try {
			String content = IOUtils.toString(resource, StandardCharsets.UTF_8);
			return content != null ? List.of(content.split("\n")) : Collections.emptyList();
		} catch (IOException e) {
			Logger.error(e);
			return Collections.emptyList();
		}
	}

}
