package io.intino.ime.box.models;

import io.intino.alexandria.Json;
import io.intino.alexandria.logger.Logger;
import io.intino.ime.box.languages.LanguageServerManager;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;
import io.intino.ime.model.Release;
import io.intino.ime.model.WorkspaceProperties;
import io.intino.languagearchetype.Archetype;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class ModelManager {
	private final Archetype archetype;
	private final LanguageServerManager serverManager;

	public ModelManager(Archetype archetype, LanguageServerManager serverManager) {
		this.archetype = archetype;
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

	public boolean exists(String id) {
		return allModels().stream().anyMatch(m -> m.id().equals(id));
	}

	public List<Model> models(Language language) {
		return allModels().stream().filter(m -> Language.nameOf(m.modelingLanguage()).equals(language.name())).toList();
	}

	public Model model(String id) {
		return modelOf(id);
	}

	public Model modelWith(Language language) {
		return modelWith(language.name());
	}

	public Model modelWith(String language) {
		return allModels().stream().filter(m -> m.label().startsWith(language)).findFirst().orElse(null);
	}

	public Model create(String id, String label, Release parent, String owner, String releasedLanguage, boolean isPrivate) {
		Model model = new Model();
		model.id(id);
		model.label(label);
		model.modelingLanguage(parent.id());
		model.releasedLanguage(releasedLanguage);
		model.owner(owner);
		model.isPrivate(isPrivate);
		createWorkspaceProperties(model, parent);
		save(model);
		return model;
	}

	public Model clone(Model model, String id, String label, String owner, String releasedLanguage) {
		Model result = Model.clone(model);
		result.id(id);
		result.label(label);
		result.owner(owner);
		result.releasedLanguage(releasedLanguage);
		save(result);
		new ModelContainerWriter(model, server(model)).clone(result, server(result));
		return modelOf(id);
	}

	public URI workspace(Model model) {
		try {
			return archetype.models().workspace(model.id()).getAbsoluteFile().getCanonicalFile().toURI();
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	public WorkspaceProperties workspaceProperties(Model model) {
		try {
			File properties = archetype.models().getWorkspaceProperties(model.id());
			if (!properties.exists()) return null;
			return Json.fromJson(Files.readString(properties.toPath()), WorkspaceProperties.class);
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	public ModelContainer.File copy(Model model, String filename, ModelContainer.File source) {
		return new ModelContainerWriter(model, server(model)).copy(filename, source);
	}

	public ModelContainer.File createFile(Model model, String name, String content, ModelContainer.File parent) {
		return new ModelContainerWriter(model, server(model)).createFile(name, content, parent);
	}

	public ModelContainer.File createFolder(Model model, String name, ModelContainer.File parent) {
		return new ModelContainerWriter(model, server(model)).createFolder(name, parent);
	}

	public void save(Model model, ModelContainer.File file, String content) {
		new ModelContainerWriter(model, server(model)).save(file, content);
	}

	public ModelContainer.File rename(Model model, ModelContainer.File file, String newName) {
		return new ModelContainerWriter(model, server(model)).rename(file, newName);
	}

	public ModelContainer.File move(Model model, ModelContainer.File file, ModelContainer.File directory) {
		try {
			return new ModelContainerWriter(model, server(model)).move(file, directory);
		} catch (Exception e) {
			Logger.error(e);
			return null;
		}
	}

	public void save(Model model) {
		try {
			Files.writeString(archetype.models().definition(model.id()).toPath(), Json.toString(model));
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private void createWorkspaceProperties(Model model, Release parent) {
		try {
			WorkspaceProperties properties = new WorkspaceProperties(parent.id());
			Files.writeString(archetype.models().getWorkspaceProperties(model.id()).toPath(), Json.toString(properties));
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public void makePrivate(Model model) {
		model.isPrivate(true);
		save(model);
	}

	public void makePublic(Model model) {
		model.isPrivate(false);
		save(model);
	}

	public void remove(Model model, ModelContainer.File file) {
		new ModelContainerWriter(model, server(model)).remove(file);
	}

	public void remove(Model model) {
		try {
			File rootDir = archetype.models().definition(model.id()).getParentFile();
			if (!rootDir.exists()) return;
			FileUtils.deleteDirectory(rootDir);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public ModelContainer modelContainer(Model model) {
		return new ModelContainer(model, server(model));
	}

	public String content(Model model, String uri) {
		return new ModelContainerReader(model, server(model)).content(uri);
	}

	private Model modelOf(File file) {
		return modelOf(file.getName());
	}

	private Model modelOf(String id) {
		try {
			File definition = archetype.models().definition(id);
			if (!definition.exists()) return null;
			return Json.fromJson(Files.readString(definition.toPath()), Model.class);
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	private boolean belongsTo(Model model, String user) {
		if (model == null) return false;
		return user != null && model.owner() != null && user.equals(model.owner());
	}

	private LanguageServer server(Model model) {
		try {
			return serverManager.get(model);
		} catch (IOException | GitAPIException | URISyntaxException e) {
			Logger.error(e);
			return null;
		}
	}

}
