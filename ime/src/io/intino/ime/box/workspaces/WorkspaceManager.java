package io.intino.ime.box.workspaces;

import io.intino.alexandria.Json;
import io.intino.alexandria.logger.Logger;
import io.intino.ime.model.Workspace;
import io.intino.languagearchetype.Archetype;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class WorkspaceManager {
	private final Archetype archetype;

	public WorkspaceManager(Archetype archetype) {
		this.archetype = archetype;
	}

	public List<Workspace> ownerWorkspaces(String user) {
		File[] root = archetype.workspaces().root().listFiles();
		if (root == null) return Collections.emptyList();
		return Arrays.stream(root).filter(File::isDirectory).map(this::workspaceOf).filter(w -> belongsTo(w, user)).collect(toList());
	}

	public List<Workspace> publicWorkspaces(String user) {
		return ownerWorkspaces(user).stream().filter(Workspace::isPublic).collect(toList());
	}

	public List<Workspace> privateWorkspaces(String user) {
		return ownerWorkspaces(user).stream().filter(Workspace::isPrivate).collect(toList());
	}

	public boolean exists(String name) {
		return archetype.workspaces().definition(name).exists();
	}

	public Workspace workspace(String name) {
		return workspaceOf(name);
	}

	public Workspace create(Workspace workspace) {
		save(workspace);
		return workspace;
	}

	public Workspace clone(Workspace workspace, String name, String title) {
		Workspace result = Workspace.clone(workspace);
		result.name(name);
		result.title(title);
		save(result);
		new WorkspaceContainerWriter(archetype.workspaces().documents(workspace.name())).clone(archetype.workspaces().documents(name));
		return workspaceOf(name);
	}

	public WorkspaceContainer.File copy(Workspace workspace, String filename, WorkspaceContainer.File source) {
		return new WorkspaceContainerWriter(archetype.workspaces().documents(workspace.name())).copy(filename, source);
	}

	public WorkspaceContainer.File createFile(Workspace workspace, String name, String content, WorkspaceContainer.File parent) {
		return new WorkspaceContainerWriter(archetype.workspaces().documents(workspace.name())).createFile(name, content, parent);
	}

	public WorkspaceContainer.File createFolder(Workspace workspace, String name, WorkspaceContainer.File parent) {
		return new WorkspaceContainerWriter(archetype.workspaces().documents(workspace.name())).createFolder(name, parent);
	}

	public void save(Workspace workspace, WorkspaceContainer.File file, String content) {
		new WorkspaceContainerWriter(archetype.workspaces().documents(workspace.name())).save(file, content);
	}

	public WorkspaceContainer.File rename(Workspace workspace, WorkspaceContainer.File file, String newName) {
		return new WorkspaceContainerWriter(archetype.workspaces().documents(workspace.name())).rename(file, newName);
	}

	public WorkspaceContainer.File move(Workspace workspace, WorkspaceContainer.File file, WorkspaceContainer.File directory) {
		return new WorkspaceContainerWriter(archetype.workspaces().documents(workspace.name())).move(file, directory);
	}

	public void saveTitle(Workspace workspace, String title) {
		workspace.title(title);
		save(workspace);
	}

	public void makePrivate(Workspace workspace, String token) {
		workspace.isPrivate(true);
		workspace.token(token);
		save(workspace);
	}

	public void makePublic(Workspace workspace) {
		workspace.isPrivate(false);
		save(workspace);
	}

	public void remove(Workspace workspace, WorkspaceContainer.File file) {
		new WorkspaceContainerWriter(archetype.workspaces().documents(workspace.name())).remove(file);
	}

	public void remove(Workspace workspace) {
		try {
			File rootDir = archetype.workspaces().definition(workspace.name()).getParentFile();
			if (!rootDir.exists()) return;
			FileUtils.deleteDirectory(rootDir);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public WorkspaceContainer workspaceContainer(Workspace workspace) {
		return new WorkspaceContainerReader(archetype.workspaces().documents(workspace.name())).read();
	}

	private Workspace workspaceOf(File file) {
		return workspaceOf(file.getName());
	}

	private Workspace workspaceOf(String name) {
		try {
			File definition = archetype.workspaces().definition(name);
			if (!definition.exists()) return null;
			Workspace workspace = Json.fromJson(Files.readString(definition.toPath()), Workspace.class);
			return workspace.documentRoot(archetype.workspaces().documents(name).toURI());
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	private void save(Workspace workspace) {
		try {
			Files.writeString(archetype.workspaces().definition(workspace.name()).toPath(), Json.toString(workspace));
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private boolean hasAccess(Workspace workspace, String user) {
		if (workspace == null) return false;
		if (workspace.isPublic()) return true;
		return belongsTo(workspace, user);
	}

	private boolean belongsTo(Workspace workspace, String user) {
		if (workspace == null) return false;
		return user != null && workspace.owner() != null && user.equals(workspace.owner().name());
	}

}
