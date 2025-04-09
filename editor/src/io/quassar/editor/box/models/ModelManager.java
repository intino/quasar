package io.quassar.editor.box.models;

import io.intino.alexandria.Json;
import io.intino.alexandria.logger.Logger;
import io.quassar.archetype.Archetype;
import io.quassar.editor.box.languages.LanguageServerManager;
import io.quassar.editor.box.util.ArchetypeHelper;
import io.quassar.editor.box.util.VersionNumberComparator;
import io.quassar.editor.box.util.ZipHelper;
import io.quassar.editor.model.*;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.lsp4j.services.LanguageServer;
import systems.intino.alexandria.datamarts.anchormap.AnchorMap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public class ModelManager {
	private final AnchorMap index;
	private final Archetype archetype;
	private final Function<GavCoordinates, Language> languageLoader;
	private final LanguageServerManager serverManager;

	public ModelManager(Archetype archetype, AnchorMap index, Function<GavCoordinates, Language> languageLoader, LanguageServerManager serverManager) {
		this.index = index;
		this.archetype = archetype;
		this.languageLoader = languageLoader;
		this.serverManager = serverManager;
	}

	public List<Model> models(Language language, String owner) {
		Set<String> result = new HashSet<>(index.search("model").with("language", language.id()).with("owner", owner).execute());
		result.addAll(index.search("model").with("language", language.id()).with("contributor", owner).execute());
		result.addAll(index.search("model").with("language", language.id()).with("is-private", "false").execute());
		return result.stream().map(id -> id.replace(":model", "")).map(this::get).toList();
	}

	public List<Model> models(String owner) {
		Set<String> result = new HashSet<>(index.search("model").with("owner", owner).execute());
		result.addAll(index.search("model").with("contributor", owner).execute());
		result.addAll(index.search("model").with("is-private", "false").execute());
		return result.stream().map(id -> id.replace(":model", "")).map(this::get).toList();
	}

	public List<Model> exampleModels(Language language, LanguageRelease release) {
		if (release != null) return release.examples().stream().map(this::get).toList();
		return language.releases().stream().map(LanguageRelease::examples).flatMap(Collection::stream).map(this::get).toList();
	}

	public boolean exists(String key) {
		return get(locate(key)) != null;
	}

	public Model get(String key) {
		try {
			String id = locate(key);
			File settings = archetype.models().settings(ArchetypeHelper.relativeModelPath(id), id);
			if (!settings.exists()) {
				cleanArchetypeTrashFor(key);
				return null;
			}
			return Json.fromJson(Files.readString(settings.toPath()), Model.class);
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	public List<String> releases(Model model) {
		List<File> releases = archetype.models().releases(ArchetypeHelper.relativeModelPath(model.id()), model.id());
		return releases.stream().map(f -> f.getName().replace(".zip", "")).sorted((o1, o2) -> VersionNumberComparator.getInstance().compare(o1, o2)).toList();
	}

	public File release(Model model, String version) {
		return archetype.models().release(ArchetypeHelper.relativeModelPath(model.id()), model.id(), version);
	}

	public Model create(String name, String title, String description, GavCoordinates language, String owner) {
		Model model = new Model();
		model.id(UUID.randomUUID().toString());
		model.name(name);
		model.title(title);
		model.description(description);
		model.language(language);
		model.owner(owner);
		model.isPrivate(true);
		model.createDate(Instant.now());
		save(model);
		return model;
	}

	public Model clone(Model model, String release, String name, String owner) {
		Model result = Model.clone(model);
		result.id(UUID.randomUUID().toString());
		result.name(name);
		result.owner(owner);
		result.createDate(Instant.now());
		save(result);
		new WorkspaceWriter(workspace(model, release), server(model, release)).clone(result, server(result, Model.DraftRelease));
		return get(name);
	}

	public boolean isWorkspaceEmpty(Model model, String release) {
		File workspace = workspace(model, release).root();
		File[] files = workspace.exists() ? workspace.listFiles() : null;
		return files == null || files.length == 0;
	}

	public Workspace workspace(Model model, String release) {
		return new Workspace(model, release, languageLoader.apply(model.language()), archetype);
	}

	public io.quassar.editor.box.models.File copy(Model model, String filename, io.quassar.editor.box.models.File source) {
		return new WorkspaceWriter(workspace(model, Model.DraftRelease), server(model, Model.DraftRelease)).copy(filename, source);
	}

	public OperationResult createRelease(Model model, String release) {
		try {
			if (isWorkspaceEmpty(model, Model.DraftRelease)) return OperationResult.Error("Workspace is empty");
			File releaseFile = archetype.models().release(ArchetypeHelper.relativeModelPath(model.id()), model.id(), release);
			ZipHelper.zip(workspace(model, Model.DraftRelease).root().toPath(), manifest(model, release), releaseFile.toPath());
			index.on(model.id(), "model").set("release", release).commit();
			return OperationResult.Success();
		} catch (Exception e) {
			Logger.error(e);
			return OperationResult.Error(e.getMessage());
		}
	}

	private String manifest(Model model, String release) {
		return Json.toString(new ModelRelease(release, model.language(), model.owner()));
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
			index.on(model.id(), "model").set("name", model.name()).set("owner", model.owner()).set("language", model.language().languageId()).set("is-private", String.valueOf(model.isPrivate())).commit();
			Files.writeString(archetype.models().settings(ArchetypeHelper.relativePath(model), model.id()).toPath(), Json.toString(model));
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
			File rootDir = archetype.models().get(ArchetypeHelper.relativePath(model), model.id());
			if (!rootDir.exists()) return;
			FileUtils.deleteDirectory(rootDir);
			// TODO JJ index.on(model.id(), "model").remove().commit();
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public ModelContainer modelContainer(Model model, String release) {
		return new ModelContainer(workspace(model, release), server(model, release));
	}

	public InputStream content(Model model, String release, String uri) {
		return new WorkspaceReader(workspace(model, release), server(model, release)).content(uri);
	}

	private LanguageServer server(Model model, String release) {
		try {
			return serverManager.get(model, release);
		} catch (IOException | GitAPIException | URISyntaxException e) {
			Logger.error(e);
			return null;
		}
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

	private void cleanArchetypeTrashFor(String id) {
		archetype.models().get(ArchetypeHelper.relativeModelPath(id), id).delete();
		String[] list = archetype.models().get(ArchetypeHelper.relativeModelPath(id)).list();
		if (list == null || list.length == 0) archetype.models().get(ArchetypeHelper.relativeModelPath(id)).delete();
	}

	private String locate(String key) {
		Map<String, String> modelMap = index.search("model").execute().stream().collect(toMap(u -> nameOf(index.get(u.replace(":model", ""), "model")), u -> u.replace(":model", "")));
		return modelMap.getOrDefault(key, key);
	}

	private String nameOf(List<String> values) {
		return values.stream().filter(v -> v.startsWith("name=")).map(v -> v.replace("name=", "")).findFirst().orElse(null);
	}

}
