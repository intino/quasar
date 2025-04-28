package io.quassar.editor.box.models;

import io.intino.alexandria.Json;
import io.intino.alexandria.logger.Logger;
import io.quassar.archetype.Archetype;
import io.quassar.editor.box.languages.LanguageServerManager;
import io.quassar.editor.box.util.ArchetypeHelper;
import io.quassar.editor.box.util.SubjectHelper;
import io.quassar.editor.box.util.VersionNumberComparator;
import io.quassar.editor.box.util.ZipHelper;
import io.quassar.editor.model.*;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.lsp4j.services.LanguageServer;
import systems.intino.datamarts.subjectstore.SubjectStore;
import systems.intino.datamarts.subjectstore.model.Subject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ModelManager {
	private final SubjectStore subjectStore;
	private final Archetype archetype;
	private final Function<GavCoordinates, Language> languageLoader;
	private final LanguageServerManager serverManager;

	public ModelManager(Archetype archetype, SubjectStore store, Function<GavCoordinates, Language> languageLoader, LanguageServerManager serverManager) {
		this.subjectStore = store;
		this.archetype = archetype;
		this.languageLoader = languageLoader;
		this.serverManager = serverManager;
	}

	public List<Model> models(Language language, String owner) {
		Map<String, Subject> ownerSubjects = mapOf(subjectStore.subjects().type(SubjectHelper.ModelType).with("language-group", language.group()).with("language-name", language.name()).with("owner", owner).with("visibility", Visibility.Private.name()).with("usage", Model.Usage.EndUser.name()).collect());
		Map<String, Subject> contributorSubjects = mapOf(subjectStore.subjects().type(SubjectHelper.ModelType).with("language-group", language.group()).with("language-name", language.name()).with("contributor", owner).with("visibility", Visibility.Private.name()).with("usage", Model.Usage.EndUser.name()).collect());
		Map<String, Subject> quassarSubjects = mapOf(subjectStore.subjects().type(SubjectHelper.ModelType).with("language-group", language.group()).with("language-name", language.name()).with("owner", User.Quassar).with("visibility", Visibility.Public.name()).with("usage", Model.Usage.EndUser.name()).collect());
		ownerSubjects.putAll(contributorSubjects);
		ownerSubjects.putAll(quassarSubjects);
		return ownerSubjects.values().stream().map(this::get).toList();
	}

	public List<Model> models(Language language) {
		Map<String, Subject> ownerSubjects = mapOf(subjectStore.subjects().type(SubjectHelper.ModelType).with("language-group", language.group()).with("language-name", language.name()).collect());
		Map<String, Subject> contributorSubjects = mapOf(subjectStore.subjects().type(SubjectHelper.ModelType).with("language-group", language.group()).with("language-name", language.name()).collect());
		Map<String, Subject> quassarSubjects = mapOf(subjectStore.subjects().type(SubjectHelper.ModelType).with("language-group", language.group()).with("language-name", language.name()).collect());
		ownerSubjects.putAll(contributorSubjects);
		ownerSubjects.putAll(quassarSubjects);
		return ownerSubjects.values().stream().map(this::get).toList();
	}

	public List<Model> models(String owner) {
		Map<String, Subject> ownerSubjects = mapOf(subjectStore.subjects().type(SubjectHelper.ModelType).with("owner", owner).with("visibility", Visibility.Private.name()).with("usage", Model.Usage.EndUser.name()).collect());
		Map<String, Subject> contributorSubjects = mapOf(subjectStore.subjects().type(SubjectHelper.ModelType).with("contributor", owner).with("visibility", Visibility.Private.name()).with("usage", Model.Usage.EndUser.name()).collect());
		Map<String, Subject> quassarSubjects = mapOf(subjectStore.subjects().type(SubjectHelper.ModelType).with("owner", User.Quassar).with("visibility", Visibility.Public.name()).with("usage", Model.Usage.EndUser.name()).collect());
		ownerSubjects.putAll(contributorSubjects);
		ownerSubjects.putAll(quassarSubjects);
		return ownerSubjects.values().stream().map(this::get).toList();
	}

	public List<Model> exampleModels(Language language, LanguageRelease release) {
		if (release != null) return release.examples().stream().map(this::get).toList();
		return language.releases().stream().map(LanguageRelease::examples).flatMap(Collection::stream).distinct().map(this::get).toList();
	}

	public boolean exists(String key) {
		return get(key) != null;
	}

	public Model get(String key) {
		Subject subject = subjectStore.open(SubjectHelper.modelPath(key));
		if (subject == null || subject.isNull()) subject = subjectStore.subjects().type(SubjectHelper.ModelType).with("name", key).collect().stream().findFirst().orElse(null);
		return subject != null && !subject.isNull() ? new Model(subject) : null;
	}

	public List<String> releases(Model model) {
		List<File> releases = archetype.models().releases(ArchetypeHelper.relativeModelPath(model.id()), model.id());
		return releases.stream().map(f -> f.getName().replace(".zip", "")).sorted((o1, o2) -> VersionNumberComparator.getInstance().compare(o1, o2)).toList();
	}

	public File release(Model model, String version) {
		return archetype.models().release(ArchetypeHelper.relativeModelPath(model.id()), model.id(), version);
	}

	public Model create(String id, String name, String title, String description, GavCoordinates language, Model.Usage usage, String owner) {
		Model model = new Model(subjectStore.create(SubjectHelper.modelPath(id)));
		model.name(name);
		model.title(title);
		model.description(description);
		model.language(language);
		model.owner(owner);
		model.isPrivate(true);
		model.usage(usage);
		model.createDate(Instant.now());
		return model;
	}

	public Model clone(Model model, String release, String id, String name, String owner) {
		Model result = new Model(subjectStore.create(SubjectHelper.modelPath(id)));
		result.name(name);
		result.language(model.language());
		result.owner(owner);
		result.isPrivate(true);
		result.usage(Model.Usage.EndUser);
		result.createDate(Instant.now());
		new WorkspaceWriter(workspace(model, release), server(model, release)).clone(result, server(result, Model.DraftRelease));
		return get(name);
	}

	public void copyWorkSpace(Model template, Model model) {
		new WorkspaceWriter(workspace(template, Model.DraftRelease), server(template, Model.DraftRelease)).clone(model, server(model, Model.DraftRelease));
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
			ModelRelease modelRelease = new ModelRelease(subjectStore.create(SubjectHelper.pathOf(model, release)));
			modelRelease.version(release);
			modelRelease.language(model.language());
			modelRelease.owner(model.owner());
			ZipHelper.zip(workspace(model, Model.DraftRelease).root().toPath(), manifest(modelRelease), releaseFile.toPath());
			return OperationResult.Success();
		} catch (Exception e) {
			Logger.error(e);
			return OperationResult.Error(e.getMessage());
		}
	}

	private String manifest(ModelRelease modelRelease) {
		return Json.toString(modelRelease);
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

	public void remove(Model model, io.quassar.editor.box.models.File file) {
		new WorkspaceWriter(workspace(model, Model.DraftRelease), server(model, Model.DraftRelease)).remove(file);
	}

	public void remove(Model model) {
		try {
			File rootDir = archetype.models().get(ArchetypeHelper.relativePath(model), model.id());
			if (!rootDir.exists()) return;
			releases(model).forEach(r -> subjectStore.open(SubjectHelper.pathOf(model, r)).drop());
			subjectStore.open(SubjectHelper.pathOf(model)).drop();
			FileUtils.deleteDirectory(rootDir);
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

	private Map<String, Subject> mapOf(List<Subject> subjects) {
		return subjects.stream().collect(Collectors.toMap(Subject::identifier, s -> s));
	}

	private Model get(Subject subject) {
		return new Model(subject);
	}

}
