package io.quassar.editor.box.models;

import io.intino.alexandria.Json;
import io.intino.alexandria.logger.Logger;
import io.quassar.archetype.Archetype;
import io.quassar.editor.box.languages.LanguageServerManager;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.box.util.VersionNumberComparator;
import io.quassar.editor.box.util.ZipHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;
import io.quassar.editor.model.OperationResult;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public class ModelManager {
	private final Archetype archetype;
	private final Function<String, Language> languageLoader;
	private final LanguageServerManager serverManager;

	public ModelManager(Archetype archetype, Function<String, Language> languageLoader, LanguageServerManager serverManager) {
		this.archetype = archetype;
		this.languageLoader = languageLoader;
		this.serverManager = serverManager;
	}

	public List<Model> visibleModels(Language language, String owner) {
		Set<Model> result = new HashSet<>(publicModels(language, owner));
		result.addAll(ownerModels(language, owner));
		return new ArrayList<>(result);
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
		return models(language).stream().filter(m -> !m.isTemplate() && belongsTo(m, user)).collect(toList());
	}

	public List<Model> publicModels(Language language, String user) {
		return publicModels(language.name(), user);
	}

	public List<Model> publicModels(String language, String user) {
		return models(language).stream().filter(m -> !m.isTemplate() && (m.isPublic() || PermissionsHelper.hasPermissions(m, user))).collect(toList());
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
			if (!new File(archetype.languages().models(Language.nameOf(language)), id).exists()) return null;
			File properties = archetype.languages().properties(Language.nameOf(language), id);
			if (!properties.exists()) return null;
			return Json.fromJson(Files.readString(properties.toPath()), Model.class);
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	public List<String> releases(Model model) {
		List<File> releases = archetype.languages().releases(languageNameOf(model), model.name());
		return releases.stream().map(f -> f.getName().replace(".zip", "")).sorted((o1, o2) -> VersionNumberComparator.getInstance().compare(o1, o2)).toList();
	}

	public File release(Model model, String version) {
		return archetype.languages().release(languageNameOf(model), model.name(), version);
	}

	public File releaseAccessor(Model model, String version) {
		return archetype.languages().releaseAccessor(languageNameOf(model), model.name(), version);
	}

	public Model create(String name, String title, String hint, String description, Language language, String owner) {
		Model model = new Model();
		model.name(name);
		model.title(title);
		model.hint(hint);
		model.description(description);
		model.language(language.name());
		model.owner(owner);
		model.isPrivate(true);
		model.createDate(Instant.now());
		save(model);
		return model;
	}

	public Model clone(Model model, String release, String name, String owner) {
		Model result = Model.clone(model);
		result.name(name);
		result.owner(owner);
		result.createDate(Instant.now());
		save(result);
		new WorkspaceWriter(workspace(model, release), server(model, release)).clone(result, server(result, Model.DraftRelease));
		return get(languageNameOf(model), name);
	}

	public boolean isWorkspaceEmpty(Model model, String release) {
		File workspace = workspace(model, release).root();
		File[] files = workspace.exists() ? workspace.listFiles() : null;
		return files == null || files.length == 0;
	}

	public Workspace workspace(Model model, String release) {
		return new Workspace(language(model), model, release, archetype);
	}

	public io.quassar.editor.box.models.File copy(Model model, String filename, io.quassar.editor.box.models.File source) {
		return new WorkspaceWriter(workspace(model, Model.DraftRelease), server(model, Model.DraftRelease)).copy(filename, source);
	}

	public OperationResult createRelease(Model model, String release, InputStream accessor) {
		try {
			if (isWorkspaceEmpty(model, Model.DraftRelease)) return OperationResult.Error("Workspace is empty");
			File releaseFile = archetype.languages().release(languageNameOf(model), model.name(), release);
			ZipHelper.zipFolder(workspace(model, Model.DraftRelease).root().toPath(), releaseFile.toPath());
			File accessorDestiny = releaseAccessor(model, release);
			FileUtils.copyInputStreamToFile(accessor, accessorDestiny);
			return OperationResult.Success();
		} catch (Exception e) {
			Logger.error(e);
			return OperationResult.Error(e.getMessage());
		}
	}

	public boolean existsFile(Model model, String name, io.quassar.editor.box.models.File parent) {
		return new WorkspaceReader(workspace(model, Model.DraftRelease), server(model, Model.DraftRelease)).existsFile(name, parent);
	}

	public io.quassar.editor.box.models.File createFile(Model model, String name, InputStream content, io.quassar.editor.box.models.File parent) {
		return new WorkspaceWriter(workspace(model, Model.DraftRelease), server(model, Model.DraftRelease)).createFile(name, content, parent);
	}

	public io.quassar.editor.box.models.File createFolder(Model model, String name, io.quassar.editor.box.models.File parent) {
		return new WorkspaceWriter(workspace(model, Model.DraftRelease), server(model, Model.DraftRelease)).createFolder(name, parent);
	}

	public void save(Model model, io.quassar.editor.box.models.File file, InputStream content) {
		new WorkspaceWriter(workspace(model, Model.DraftRelease), server(model, Model.DraftRelease)).save(file, content);
	}

	public io.quassar.editor.box.models.File rename(Model model, io.quassar.editor.box.models.File file, String newName) {
		return new WorkspaceWriter(workspace(model, Model.DraftRelease), server(model, Model.DraftRelease)).rename(file, newName);
	}

	public io.quassar.editor.box.models.File move(Model model, io.quassar.editor.box.models.File file, io.quassar.editor.box.models.File directory) {
		try {
			return new WorkspaceWriter(workspace(model, Model.DraftRelease), server(model, Model.DraftRelease)).move(file, directory);
		} catch (Exception e) {
			Logger.error(e);
			return null;
		}
	}

	public void save(Model model) {
		try {
			Files.writeString(archetype.languages().properties(languageNameOf(model), model.name()).toPath(), Json.toString(model));
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

	public void remove(Model model, io.quassar.editor.box.models.File file) {
		new WorkspaceWriter(workspace(model, Model.DraftRelease), server(model, Model.DraftRelease)).remove(file);
	}

	public void remove(Model model) {
		try {
			File rootDir = archetype.languages().model(languageNameOf(model), model.name());
			if (!rootDir.exists()) return;
			FileUtils.deleteDirectory(rootDir);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public ModelContainer modelContainer(Model model, String release) {
		return new ModelContainer(workspace(model, release), server(model, release));
	}

	public InputStream content(Language language, Model model, String release, String uri) {
		return new WorkspaceReader(workspace(model, release), server(model, release)).content(uri);
	}

	private boolean belongsTo(Model model, String user) {
		if (model == null) return false;
		return user != null && model.owner() != null && user.equals(model.owner());
	}

	private LanguageServer server(Model model, String release) {
		try {
			return serverManager.get(model, release);
		} catch (IOException | GitAPIException | URISyntaxException e) {
			Logger.error(e);
			return null;
		}
	}

	private Language language(Model model) {
		return languageLoader.apply(languageNameOf(model));
	}

	private String languageNameOf(Model model) {
		return Language.nameOf(model.language());
	}

	public boolean isTextFile(Model model, String release, io.quassar.editor.box.models.File file) {
		if (!file.isResource()) return true;
		return isText(new File(workspace(model, release).root(), file.uri()));
	}

	private static final List<String> TextContentTypes = List.of("text/", "application/json", "application/xml", "application/javascript", "application/xhtml+xml");
	private boolean isText(java.io.File file) {
		try {
			if (file.length() == 0) return true;
			Tika tika = new Tika();
			String contentType = tika.detect(file);
			return TextContentTypes.stream().anyMatch(contentType::startsWith);
		} catch (IOException ignored) {
			return false;
		}
	}

}
