package io.intino.ime.box.models;

import io.intino.alexandria.logger.Logger;
import io.intino.ime.box.util.ModelHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModelContainerReader {
	private final File root;

	public ModelContainerReader(File root) {
		this.root = root;
	}

	public ModelContainer read() {
		ModelContainer result = new ModelContainer();
		if (!root.exists()) return result;
		try (Stream<Path> walk = Files.walk(root.toPath())) {
			register(walk.filter(f -> !f.toFile().getAbsolutePath().equals(root.getAbsolutePath()) && !f.toFile().getName().equals(".DS_Store")).collect(Collectors.toList()), result);
		} catch (IOException e) {
			Logger.error(e);
		}
		return result;
	}

	private void register(List<Path> pathList, ModelContainer structure) {
		structure.add(pathList.stream().map(p -> ModelHelper.fileOf(root, p)).toList());
	}

}
