package io.intino.ime.box.models;

import io.intino.alexandria.Json;
import io.intino.alexandria.logger.Logger;
import io.intino.ime.box.languages.LanguageManager;
import io.intino.ime.box.languages.LanguageServerManager;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;
import io.intino.ime.model.User;
import io.intino.languagearchetype.Archetype;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class ModelManager {
	private final Archetype archetype;
	private final LanguageManager languageManager;
	private final LanguageServerManager serverManager;

	public ModelManager(Archetype archetype, LanguageManager languageManager, LanguageServerManager serverManager) {
		this.archetype = archetype;
		this.languageManager = languageManager;
		this.serverManager = serverManager;
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
		if (languageManager.existsWithName(name)) return true;
		return allModels().stream().anyMatch(w -> w.name().equals(name));
	}

	public Model model(String id) {
		Model model = modelOf(Model.nameOf(id));
		return model != null ? model.version(Model.versionOf(id)) : null;
	}

	public Model model(String name, String version) {
		Model model = modelOf(name);
		return model != null ? model.version(version) : null;
	}

	public Model create(Model model, String metaLanguageVersion) {
		model.add(new Model.Version("1.0.0", metaLanguageVersion, ""));
		save(model);
		return model;
	}

	public Model createVersion(Model model, Model.Version version) {
		Model result = Model.clone(model);
		result.version(version.id());
		result.language(Language.id(Language.nameOf(model.language()), version.metamodelVersion()));
		result.lastModifyDate(Instant.now());
		result.versionMap(model.versionMap());
		result.add(version);
		save(result);
		return result;
	}

	public Model saveVersion(Model model, Model.Version version) {
		Model.Version modelVersion = model.versionMap().get(version.id());
		modelVersion.metamodelVersion(version.metamodelVersion());
		modelVersion.builderUrl(version.builderUrl());
		save(model);
		return model;
	}

	public Model clone(Model model, Language metaLanguage, String name, String title, User owner) {
		try {
			Model result = Model.clone(model);
			result.name(name);
			result.version("1.0.0");
			result.title(title);
			result.owner(owner);
			result.add(new Model.Version("1.0.0", metaLanguage.version(), ""));
			save(result);
			new ModelContainerWriter(model, serverManager.get(model)).clone(result, serverManager.get(result));
			return modelOf(name);
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	public URI workspace(Model model) {
		try {
			return archetype.models().workspace(model.name(), model.version()).getAbsoluteFile().getCanonicalFile().toURI();
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	public ModelContainer.File copy(Model model, String filename, ModelContainer.File source) {
		try {
			return new ModelContainerWriter(model, serverManager.get(model)).copy(filename, source);
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	public ModelContainer.File createFile(Model model, String name, String content, ModelContainer.File parent) {
		try {
			return new ModelContainerWriter(model, serverManager.get(model)).createFile(name, content, parent);
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	public ModelContainer.File createFolder(Model model, String name, ModelContainer.File parent) {
		try {
			return new ModelContainerWriter(model, serverManager.get(model)).createFolder(name, parent);
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	public void save(Model model, ModelContainer.File file, String content) {
		try {
			new ModelContainerWriter(model, serverManager.get(model)).save(file, content);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public ModelContainer.File rename(Model model, ModelContainer.File file, String newName) {
		try {
			return new ModelContainerWriter(model, serverManager.get(model)).rename(file, newName);
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	public ModelContainer.File move(Model model, ModelContainer.File file, ModelContainer.File directory) {
		try {
			return new ModelContainerWriter(model, serverManager.get(model)).move(file, directory);
		} catch (Exception e) {
			Logger.error(e);
			return null;
		}
	}

	public void save(Model model) {
		try {
			Files.writeString(archetype.models().definition(model.name()).toPath(), Json.toString(model));
		} catch (IOException e) {
			Logger.error(e);
		}
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
		try {
			new ModelContainerWriter(model, serverManager.get(model)).remove(file);
		} catch (IOException e) {
			Logger.error(e);
		}
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
		try {
			return new ModelContainer(model, serverManager.get(model));
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	public String content(Model model, String uri) {
		try {
			return new ModelContainerReader(model, serverManager.get(model)).content(uri);
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	private Model modelOf(File file) {
		return modelOf(file.getName());
	}

	private Model modelOf(String name) {
		try {
			File definition = archetype.models().definition(name);
			if (!definition.exists()) return null;
			return Json.fromJson(Files.readString(definition.toPath()), Model.class);
		} catch (IOException e) {
			Logger.error(e);
			return null;
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
