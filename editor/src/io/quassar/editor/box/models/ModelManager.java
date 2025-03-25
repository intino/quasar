package io.quassar.editor.box.models;

import io.intino.alexandria.Json;
import io.intino.alexandria.logger.Logger;
import io.quassar.archetype.Archetype;
import io.quassar.editor.box.languages.LanguageServerManager;
import io.quassar.editor.box.util.VersionNumberComparator;
import io.quassar.editor.box.util.ZipHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;
import io.quassar.editor.model.OperationResult;
import io.quassar.editor.model.Project;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

	public List<Model> models(Language language) {
		return models(language.name());
	}

	public List<Model> models(String language) {
		File[] root = archetype.languages().models(Language.nameOf(language)).listFiles();
		if (root == null) return Collections.emptyList();
		return Arrays.stream(root).filter(File::isDirectory).map(d -> get(language, d.getName())).filter(Objects::nonNull).collect(toList());
	}

	public List<Model> ownerModels(Language language, String user) {
		return ownerModels(language.name(), user);
	}

	public List<Model> ownerModels(String language, String user) {
		return models(language).stream().filter(w -> belongsTo(w, user)).collect(toList());
	}

	public List<Model> publicModels(Language language) {
		return publicModels(language.name());
	}

	public List<Model> publicModels(String language) {
		return models(language).stream().filter(Model::isPublic).collect(toList());
	}

	public List<Model> privateModels(Language language, String user) {
		return privateModels(language.name(), user);
	}

	public List<Model> privateModels(String language, String user) {
		return ownerModels(language, user).stream().filter(Model::isPrivate).collect(toList());
	}

	public boolean exists(Language language, String name) {
		return exists(language.name(), name);
	}

	public boolean exists(String language, String name) {
		return models(language).stream().anyMatch(m -> m.name().equals(name));
	}

	public Model get(Language language, String id) {
		return get(language.name(), id);
	}

	public Model get(String language, String id) {
		try {
			File properties = archetype.languages().properties(Language.nameOf(language), id);
			if (!properties.exists()) return null;
			return Json.fromJson(Files.readString(properties.toPath()), Model.class);
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	public List<String> versions(Model model) {
		List<File> versions = archetype.languages().versions(languageOf(model), model.name());
		return versions.stream().map(f -> f.getName().replace(".zip", "")).sorted((o1, o2) -> VersionNumberComparator.getInstance().compare(o1, o2)).toList();
	}

	public File version(Model model, String version) {
		return archetype.languages().version(languageOf(model), model.name(), version);
	}

	public Model create(String name, String title, String description, Language language, String owner) {
		Model model = new Model();
		model.name(name);
		model.title(title);
		model.description(description);
		model.language(language.name());
		model.owner(owner);
		model.isPrivate(true);
		save(model);
		return model;
	}

	public Model clone(Model model, String name, String owner) {
		Model result = Model.clone(model);
		result.name(name);
		result.owner(owner);
		save(result);
		new ModelContainerWriter(model, server(model, Model.DraftVersion)).clone(result, server(result, Model.DraftVersion));
		return get(languageOf(model), name);
	}

	public boolean isWorkspaceEmpty(Model model, String version) {
		File workspace = new File(workspace(model, version));
		File[] files = workspace.exists() ? workspace.listFiles() : null;
		return files == null || files.length == 0;
	}

	public URI workspace(Model model, String version) {
		try {
			File workspace = archetype.languages().workspace(languageOf(model), model.name());
			if (version != null && !version.equals(Model.DraftVersion)) workspace = versionWorkSpace(model, version);
			return workspace.getAbsoluteFile().getCanonicalFile().toURI();
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	private File versionWorkSpace(Model model, String version) {
		File versionFile = archetype.languages().version(languageOf(model), model.name(), version);
		File workspace = archetype.tmp().versionWorkspace(languageOf(model), model.name(), version);
		if (!versionFile.exists()) return workspace;
		File[] files = workspace.listFiles();
		if (files != null && files.length > 0) return workspace;
		ZipHelper.extract(archetype.languages().version(languageOf(model), model.name(), version), workspace);
		return workspace;
	}

	public ModelContainer.File copy(Model model, String filename, ModelContainer.File source) {
		return new ModelContainerWriter(model, server(model, Model.DraftVersion)).copy(filename, source);
	}

	public OperationResult createVersion(Model model, String version) {
		try {
			if (isWorkspaceEmpty(model, Model.DraftVersion)) return OperationResult.Error("Workspace is empty");
			File versionFile = archetype.languages().version(languageOf(model), model.name(), version);
			ZipHelper.zipFolder(Paths.get(workspace(model, Model.DraftVersion)), versionFile.toPath());
			return OperationResult.Success();
		} catch (Exception e) {
			Logger.error(e);
			return OperationResult.Error(e.getMessage());
		}
	}

	public ModelContainer.File createFile(Model model, String name, String content, ModelContainer.File parent) {
		return new ModelContainerWriter(model, server(model, Model.DraftVersion)).createFile(name, content, parent);
	}

	public ModelContainer.File createFolder(Model model, String name, ModelContainer.File parent) {
		return new ModelContainerWriter(model, server(model, Model.DraftVersion)).createFolder(name, parent);
	}

	public void save(Model model, ModelContainer.File file, String content) {
		new ModelContainerWriter(model, server(model, Model.DraftVersion)).save(file, content);
	}

	public ModelContainer.File rename(Model model, ModelContainer.File file, String newName) {
		return new ModelContainerWriter(model, server(model, Model.DraftVersion)).rename(file, newName);
	}

	public ModelContainer.File move(Model model, ModelContainer.File file, ModelContainer.File directory) {
		try {
			return new ModelContainerWriter(model, server(model, Model.DraftVersion)).move(file, directory);
		} catch (Exception e) {
			Logger.error(e);
			return null;
		}
	}

	public void save(Model model) {
		try {
			Files.writeString(archetype.languages().properties(languageOf(model), model.name()).toPath(), Json.toString(model));
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
		new ModelContainerWriter(model, server(model, Model.DraftVersion)).remove(file);
	}

	public void remove(Model model) {
		try {
			File rootDir = archetype.languages().model(languageOf(model), model.name());
			if (!rootDir.exists()) return;
			FileUtils.deleteDirectory(rootDir);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public ModelContainer modelContainer(Model model, String version) {
		return new ModelContainer(model, server(model, version));
	}

	public String content(Model model, String version, String uri) {
		return new ModelContainerReader(model, server(model, version)).content(uri);
	}

	private boolean belongsTo(Model model, String user) {
		if (model == null) return false;
		return user != null && model.owner() != null && user.equals(model.owner());
	}

	private LanguageServer server(Model model, String version) {
		try {
			return serverManager.get(model, version);
		} catch (IOException | GitAPIException | URISyntaxException e) {
			Logger.error(e);
			return null;
		}
	}

	private String languageOf(Model model) {
		return Language.nameOf(model.language());
	}

}
