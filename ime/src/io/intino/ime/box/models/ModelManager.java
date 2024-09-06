package io.intino.ime.box.models;

import io.intino.alexandria.Json;
import io.intino.alexandria.logger.Logger;
import io.intino.ime.box.languages.LanguageManager;
import io.intino.ime.model.Model;
import io.intino.languagearchetype.Archetype;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class ModelManager {
	private final Archetype archetype;
	private final LanguageManager languageManager;

	public ModelManager(Archetype archetype, LanguageManager languageManager) {
		this.archetype = archetype;
		this.languageManager = languageManager;
	}

	public List<Model> allModels() {
		File[] root = archetype.models().root().listFiles();
		if (root == null) return Collections.emptyList();
		return Arrays.stream(root).filter(File::isDirectory).map(this::modelOf).filter(Objects::nonNull).collect(toList());
	}

	public List<Model> ownerModels(String user) {
		return allModels().stream().filter(w -> belongsTo(w, user)).collect(toList());
	}

	public List<Model> publicModels(String user) {
		return ownerModels(user).stream().filter(Model::isPublic).collect(toList());
	}

	public List<Model> privateModels(String user) {
		return ownerModels(user).stream().filter(Model::isPrivate).collect(toList());
	}

	public boolean exists(String name) {
		if (languageManager.exists(name)) return true;
		return allModels().stream().anyMatch(w -> w.name().equals(name));
	}

	public Model model(String name) {
		return modelOf(name);
	}

	public Model create(Model model) {
		save(model);
		return model;
	}

	public Model clone(Model model, String name, String title) {
		Model result = Model.clone(model);
		result.name(name);
		result.title(title);
		save(result);
		new ModelContainerWriter(archetype.models().documents(model.name())).clone(archetype.models().documents(name));
		return modelOf(name);
	}

	public ModelContainer.File copy(Model model, String filename, ModelContainer.File source) {
		return new ModelContainerWriter(archetype.models().documents(model.name())).copy(filename, source);
	}

	public ModelContainer.File createFile(Model model, String name, String content, ModelContainer.File parent) {
		return new ModelContainerWriter(archetype.models().documents(model.name())).createFile(name, content, parent);
	}

	public ModelContainer.File createFolder(Model model, String name, ModelContainer.File parent) {
		return new ModelContainerWriter(archetype.models().documents(model.name())).createFolder(name, parent);
	}

	public void save(Model model, ModelContainer.File file, String content) {
		new ModelContainerWriter(archetype.models().documents(model.name())).save(file, content);
	}

	public ModelContainer.File rename(Model model, ModelContainer.File file, String newName) {
		return new ModelContainerWriter(archetype.models().documents(model.name())).rename(file, newName);
	}

	public ModelContainer.File move(Model model, ModelContainer.File file, ModelContainer.File directory) {
		return new ModelContainerWriter(archetype.models().documents(model.name())).move(file, directory);
	}

	public void saveTitle(Model model, String title) {
		model.title(title);
		save(model);
	}

	public void makePrivate(Model model, String token) {
		model.isPrivate(true);
		model.token(token);
		save(model);
	}

	public void makePublic(Model model) {
		model.isPrivate(false);
		save(model);
	}

	public void remove(Model model, ModelContainer.File file) {
		new ModelContainerWriter(archetype.models().documents(model.name())).remove(file);
	}

	public void remove(Model model) {
		try {
			File rootDir = archetype.models().definition(model.name()).getParentFile();
			if (!rootDir.exists()) return;
			FileUtils.deleteDirectory(rootDir);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public ModelContainer modelContainer(Model model) {
		return new ModelContainerReader(archetype.models().documents(model.name())).read();
	}

	private Model modelOf(File file) {
		return modelOf(file.getName());
	}

	private Model modelOf(String name) {
		try {
			File definition = archetype.models().definition(name);
			if (!definition.exists()) return null;
			Model model = Json.fromJson(Files.readString(definition.toPath()), Model.class);
			return model.documentRoot(archetype.models().documents(name).getAbsoluteFile().getCanonicalFile().toURI());
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	private void save(Model model) {
		try {
			Files.writeString(archetype.models().definition(model.name()).toPath(), Json.toString(model));
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private boolean hasAccess(Model model, String user) {
		if (model == null) return false;
		if (model.isPublic()) return true;
		return belongsTo(model, user);
	}

	private boolean belongsTo(Model model, String user) {
		if (model == null) return false;
		return user != null && model.owner() != null && user.equals(model.owner().name());
	}

}
