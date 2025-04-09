package io.quassar.editor.box.commands.model;

import io.intino.alexandria.logger.Logger;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.ui.types.ModelView;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.box.util.ZipHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

public class AddModelZipCommand extends Command<Boolean> {
	public Model model;
	public ModelView view;
	public InputStream content;
	public io.quassar.editor.box.models.File parent;

	public AddModelZipCommand(EditorBox box) {
		super(box);
	}

	@Override
	public Boolean execute() {
		File directory = extract();
		String extension = languageFileExtension();
		try(Stream<Path> paths = Files.walk(directory.toPath())) {
			paths.filter(p -> p.toFile().isFile() && matches(p.toFile().getName(), extension)).forEach(p -> add(p, directory));
		} catch (IOException e) {
			Logger.error(e);
			return false;
		}
		return true;
	}

	private void add(Path path, File root) {
		try {
			File file = path.toFile();
			InputStream content = new ByteArrayInputStream(Files.readAllBytes(path));
			String filename = nameFor(file.getAbsolutePath().replace(root.getAbsolutePath() + File.separator, ""));
			if (box.modelManager().existsFile(model, filename, parent)) return;
			box.modelManager().createFile(model, filename, content, parent);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private String nameFor(String name) {
		String result = ModelHelper.validWorkspaceFileName(name);
		return view == ModelView.Resources ? parent != null ? result : io.quassar.editor.box.models.File.withResourcesPath(result) : result;
	}

	private File extract() {
		File destiny = box.archetype().tmp().upload(UUID.randomUUID().toString());
		ZipHelper.extract(content, destiny);
		return destiny;
	}

	private boolean matches(String name, String extension) {
		if (view == ModelView.Resources) return true;
		return name.endsWith("." + extension);
	}

	private String languageFileExtension() {
		return Language.FileExtension.replace("\\.", "");
	}

}
