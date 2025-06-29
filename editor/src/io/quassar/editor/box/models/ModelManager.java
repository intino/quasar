package io.quassar.editor.box.models;

import io.intino.alexandria.Json;
import io.intino.alexandria.logger.Logger;
import io.intino.ls.IntinoLanguageServer;
import io.intino.ls.codeinsight.DiagnosticService;
import io.quassar.archetype.Archetype;
import io.quassar.editor.box.languages.LanguageServerManager;
import io.quassar.editor.box.util.*;
import io.quassar.editor.model.*;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.services.LanguageServer;
import systems.intino.datamarts.subjectstore.SubjectQuery;
import systems.intino.datamarts.subjectstore.SubjectStore;
import systems.intino.datamarts.subjectstore.model.Subject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

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
		Map<String, Subject> ownerSubjects = mapOf(with(subjectStore.query().isType(SubjectHelper.ModelType), "dsl-collection", language.collection()).where("dsl-name").equals(language.name()).where("owner").equals(owner).where("usage").equals(Model.Usage.EndUser.name()).collect());
		Map<String, Subject> contributorSubjects = mapOf(with(subjectStore.query().isType(SubjectHelper.ModelType),"dsl-collection", language.collection()).where("dsl-name").equals(language.name()).where("collaborator").equals(owner).where("usage").equals(Model.Usage.EndUser.name()).collect());
		ownerSubjects.putAll(contributorSubjects);
		return ownerSubjects.values().stream().map(this::get).toList();
	}

	public List<Model> models(Language language) {
		return models(language.collection(), language.name());
	}

	public List<Model> models(String collection, String language) {
		Map<String, Subject> ownerSubjects = mapOf(with(subjectStore.query().isType(SubjectHelper.ModelType),"dsl-collection", collection).where("dsl-name").equals(language).collect());
		Map<String, Subject> contributorSubjects = mapOf(with(subjectStore.query().isType(SubjectHelper.ModelType),"dsl-collection", collection).where("dsl-name").equals(language).collect());
		ownerSubjects.putAll(contributorSubjects);
		return ownerSubjects.values().stream().map(this::get).toList();
	}

	public List<Model> models(String owner) {
		Map<String, Subject> ownerSubjects = mapOf(subjectStore.query().isType(SubjectHelper.ModelType).where("owner").equals(owner).where("usage").equals(Model.Usage.EndUser.name()).collect());
		Map<String, Subject> contributorSubjects = mapOf(subjectStore.query().isType(SubjectHelper.ModelType).where("collaborator").equals(owner).where("usage").equals(Model.Usage.EndUser.name()).collect());
		ownerSubjects.putAll(contributorSubjects);
		return ownerSubjects.values().stream().map(this::get).toList();
	}

	public List<Model> exampleModels(Language language, LanguageRelease release) {
		if (release != null) return release.examples().stream().map(this::get).toList();
		return language.releases().stream().map(LanguageRelease::examples).flatMap(Collection::stream).distinct().map(this::get).toList();
	}

	public List<Model> models(String project, String module, String owner) {
		List<Subject> result = subjectStore.query().isType(SubjectHelper.ModelType).where("owner").equals(owner).where("project").equals(project).where("module").equals(module).collect();
		return result.stream().map(this::get).toList();
	}

	public List<String> projects(String owner) {
		List<Subject> result = subjectStore.query().isType(SubjectHelper.ModelType).where("owner").equals(owner).collect();
		return result.stream().map(s -> s.get("project")).filter(r -> r != null && !r.isEmpty()).distinct().toList();
	}

	public List<String> modules(String project, String owner) {
		if (project == null) return emptyList();
		List<Subject> result = subjectStore.query().isType(SubjectHelper.ModelType).where("owner").equals(owner).where("project").equals(project).collect();
		return result.stream().map(s -> s.get("module")).filter(r -> r != null && !r.isEmpty()).distinct().toList();
	}

	public boolean exists(String key) {
		return get(key) != null;
	}

	public Model find(String commit) {
		Subject subject = subjectStore.query().isType(SubjectHelper.ModelReleaseType).where("commit").equals(commit).collect().stream().findFirst().orElse(null);
		if (subject == null || subject.isNull()) return null;
		return get(subject.parent());
	}

	public Model get(String key) {
		if (key == null) return null;
		Subject subject = subjectStore.open(SubjectHelper.modelPath(key));
		if (subject == null || subject.isNull()) subject = subjectStore.query().isType(SubjectHelper.ModelType).where("name").equals(key).collect().stream().findFirst().orElse(null);
		return get(subject);
	}

	public Model getTemplate(Language language, LanguageRelease release) {
		return get(subjectStore.query().isType(SubjectHelper.ModelType).where("usage").equals(Model.Usage.Template.name()).where("dsl-collection").equals(language.collection()).where("dsl-name").equals(language.name()).where("dsl-version").equals(release.version()).collect().stream().findFirst().orElse(null));
	}

	public List<String> releases(Model model) {
		List<File> releases = archetype.models().releases(ArchetypeHelper.relativeModelPath(model.id()), model.id());
		return releases.stream().map(f -> f.getName().replace(".zip", "")).sorted((o1, o2) -> VersionNumberComparator.getInstance().compare(o1, o2)).toList();
	}

	public File release(Model model, String version) {
		return archetype.models().release(ArchetypeHelper.relativeModelPath(model.id()), model.id(), version);
	}

	public ModelRelease findRelease(String commit) {
		return releaseOf(subjectStore.query().isType(SubjectHelper.ModelReleaseType).where("commit").equals(commit).collect().stream().findFirst().orElse(null));
	}

	public Model create(String id, String name, String title, String description, GavCoordinates language, Model.Usage usage, String owner) {
		Model model = new Model(subjectStore.create(SubjectHelper.modelPath(id)));
		model.name(name);
		model.title(title.toUpperCase());
		model.description(description);
		model.language(language);
		model.owner(owner);
		model.isPrivate(true);
		model.usage(usage);
		model.createDate(Instant.now());
		model.updateDate(Instant.now());
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
		model.updateDate(Instant.now());
		new WorkspaceWriter(workspace(model, release), server(model, release)).clone(result, server(result, Model.DraftRelease));
		return get(id);
	}

	public List<Diagnostic> check(Model model, String release) {
		IntinoLanguageServer server = server(model, release);
		if (server == null) return emptyList();
		DiagnosticService diagnosticService = server.getDiagnosticService();
		if (diagnosticService == null) return emptyList();
		return diagnosticService.analyzeWorkspace();
	}

	public void updateLanguageVersion(Model model, String version) {
		GavCoordinates language = model.language();
		model.language(new GavCoordinates(language.groupId(), language.artifactId(), version));
		serverManager.remove(model, Model.DraftRelease);
	}

	public void removeLanguageServer(Model model) {
		serverManager.remove(model, Model.DraftRelease);
	}

	public void copyWorkSpace(Model template, Model model) {
		new WorkspaceWriter(workspace(template, Model.DraftRelease), server(template, Model.DraftRelease)).clone(model, server(model, Model.DraftRelease));
	}

	public boolean isWorkspaceEmpty(Model model, String release) {
		File workspace = workspace(model, release).root();
		File[] files = workspace.exists() ? workspace.listFiles() : null;
		return files == null || files.length == 0;
	}

	public boolean hasWorkspaceMograms(Model model, String release) {
		IntinoLanguageServer server = server(model, release);
		if (server == null) return false;
		DiagnosticService.Statistics statistics = server.getDiagnosticService().statistics();
		return statistics.mogramsPerUnit().values().stream().mapToLong(v -> v).sum() > 0;
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
			modelRelease.commit(UUID.randomUUID().toString());
			modelRelease.version(release);
			modelRelease.language(model.language());
			modelRelease.owner(model.owner());
			ZipHelper.zip(workspace(model, Model.DraftRelease).root().toPath(), manifest(model, modelRelease), releaseFile.toPath());
			return OperationResult.Success();
		} catch (Exception e) {
			Logger.error(e);
			return OperationResult.Error(e.getMessage());
		}
	}

	public OperationResult replaceRelease(Model model, String release) {
		try {
			if (isWorkspaceEmpty(model, Model.DraftRelease)) return OperationResult.Error("Workspace is empty");
			File releaseFile = archetype.models().release(ArchetypeHelper.relativeModelPath(model.id()), model.id(), release);
			ModelRelease modelRelease = model.release(release);
			ZipHelper.zip(workspace(model, Model.DraftRelease).root().toPath(), manifest(model, modelRelease), releaseFile.toPath());
			return OperationResult.Success();
		} catch (Exception e) {
			Logger.error(e);
			return OperationResult.Error(e.getMessage());
		}
	}

	public void renameRelease(Model model, ModelRelease release, String newVersion) {
		ModelRelease newRelease = new ModelRelease(subjectStore.open(SubjectHelper.pathOf(model, release.version())).rename(newVersion));
		newRelease.version(newVersion);
	}

	public OperationResult removeRelease(Model model, String release) {
		try {
			Subject subject = subjectStore.open(SubjectHelper.pathOf(model, release));
			if (subject != null) subject.drop();
			File releaseFile = archetype.models().release(ArchetypeHelper.relativeModelPath(model.id()), model.id(), release);
			if (releaseFile.exists()) releaseFile.delete();
			return OperationResult.Success();
		} catch (Exception e) {
			Logger.error(e);
			return OperationResult.Error(e.getMessage());
		}
	}

	private String manifest(Model model, ModelRelease modelRelease) {
		Manifest manifest = new Manifest();
		manifest.id(model.id());
		manifest.name(model.isTitleQualified() ? model.qualifiedTitle() : model.title());
		manifest.commit(modelRelease.commit());
		manifest.version(modelRelease.version());
		manifest.dsl(model.language().toString());
		manifest.owner(model.owner());
		return Json.toString(manifest);
	}

	public boolean existsFile(Model model, String name, io.quassar.editor.box.models.File parent) {
		return new WorkspaceReader(workspace(model, Model.DraftRelease), server(model, Model.DraftRelease)).existsFile(name, parent);
	}

	public io.quassar.editor.box.models.File createFile(Model model, String name, InputStream content, io.quassar.editor.box.models.File parent) {
		model.updateDate(Instant.now());
		return new WorkspaceWriter(workspace(model, Model.DraftRelease), server(model, Model.DraftRelease)).createFile(name, content, parent);
	}

	public io.quassar.editor.box.models.File createFolder(Model model, String name, io.quassar.editor.box.models.File parent) {
		model.updateDate(Instant.now());
		return new WorkspaceWriter(workspace(model, Model.DraftRelease), server(model, Model.DraftRelease)).createFolder(name, parent);
	}

	public void save(Model model, io.quassar.editor.box.models.File file, InputStream content) {
		model.updateDate(Instant.now());
		new WorkspaceWriter(workspace(model, Model.DraftRelease), server(model, Model.DraftRelease)).save(file, content);
	}

	public io.quassar.editor.box.models.File rename(Model model, io.quassar.editor.box.models.File file, String newName) {
		model.updateDate(Instant.now());
		return new WorkspaceWriter(workspace(model, Model.DraftRelease), server(model, Model.DraftRelease)).rename(file, newName);
	}

	public io.quassar.editor.box.models.File move(Model model, io.quassar.editor.box.models.File file, io.quassar.editor.box.models.File directory) {
		try {
			model.updateDate(Instant.now());
			return new WorkspaceWriter(workspace(model, Model.DraftRelease), server(model, Model.DraftRelease)).move(file, directory);
		} catch (Exception e) {
			Logger.error(e);
			return null;
		}
	}

	public void remove(Model model, io.quassar.editor.box.models.File file) {
		model.updateDate(Instant.now());
		new WorkspaceWriter(workspace(model, Model.DraftRelease), server(model, Model.DraftRelease)).remove(file);
	}

	public void remove(String model) {
		remove(get(model));
	}

	public void remove(Model model) {
		try {
			releases(model).forEach(r -> subjectStore.open(SubjectHelper.pathOf(model, r)).drop());
			subjectStore.open(SubjectHelper.pathOf(model)).drop();
			File rootDir = archetype.models().get(ArchetypeHelper.relativePath(model), model.id());
			if (!rootDir.exists()) return;
			FileUtils.deleteDirectory(rootDir);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public ModelContainer modelContainer(Model model, String release) {
		LanguageServer server = server(model, release);
		if (server == null) return null;
		return new ModelContainer(workspace(model, release), server);
	}

	public InputStream content(Model model, String release, String uri) {
		return new WorkspaceReader(workspace(model, release), server(model, release)).content(uri);
	}

	public List<Model> modelsWithRelease(Language language, String release) {
		if (language == null || release == null) return emptyList();
		List<Subject> subjects = subjectStore.query().isType(SubjectHelper.ModelType).where("dsl-collection").equals(language.collection()).where("dsl-name").equals(language.name()).where("dsl-version").equals(release).collect();
		return subjects.stream().map(this::get).filter(Objects::nonNull).toList();
	}

	public boolean existsModelsWithReleasesFor(Language language, String release) {
		if (language == null || release == null) return false;
		List<Model> models = modelsWithRelease(language, release).stream().filter(m -> m.usage() == Model.Usage.EndUser).toList();
		if (models.isEmpty()) return false;
		return models.stream().noneMatch(m -> m.releases().size() <= 1);
	}

	private IntinoLanguageServer server(Model model, String release) {
		try {
			return (IntinoLanguageServer) serverManager.get(model, release);
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
		return subject != null && !subject.isNull() ? new Model(subject) : null;
	}

	private ModelRelease releaseOf(Subject subject) {
		return subject != null && !subject.isNull() ? new ModelRelease(subject) : null;
	}

	private SubjectQuery with(SubjectQuery query, String name, String value) {
		if (value.isEmpty()) return query;
		return query.where(name).equals(value);
	}

}
