package io.intino.ime.box.workspaces;

import io.intino.alexandria.Json;
import io.intino.alexandria.logger.Logger;
import io.intino.languagearchetype.Archetype;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class WorkspaceManager {
	private final Archetype archetype;

	public WorkspaceManager(Archetype archetype) {
		this.archetype = archetype;
	}

	public List<Workspace> allWorkspaces(String user) {
		File[] root = archetype.workspaces().root().listFiles();
		if (root == null) return Collections.emptyList();
		return Arrays.stream(root).filter(File::isDirectory).map(this::workspaceOf).filter(Objects::nonNull).collect(toList());
	}

	public List<Workspace> ownerWorkspaces(String user) {
		return allWorkspaces(user).stream().filter(w -> w.name().equals("w1")).collect(toList());
	}

	public List<Workspace> sharedWorkspaces(String user) {
		return allWorkspaces(user).stream().filter(w -> w.name().equals("w2") || w.name().equals("w3")).collect(toList());
	}

	public Workspace workspace(String name) {
		return workspaceOf(name);
	}

	public Workspace create(Workspace workspace) {
		try {
			Files.writeString(archetype.workspaces().definition(workspace.name()).toPath(), Json.toString(workspace));
			return workspace;
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	public WorkspaceContainer.File create(Workspace workspace, String filename, WorkspaceContainer.File parent) {
		return new WorkspaceContainerWriter(archetype.workspaces().documents(workspace.name())).create(filename, parent);
	}

	public void save(Workspace workspace, WorkspaceContainer.File file, String content) {
		new WorkspaceContainerWriter(archetype.workspaces().documents(workspace.name())).save(file, content);
	}

	public void remove(Workspace workspace, WorkspaceContainer.File file) {
		new WorkspaceContainerWriter(archetype.workspaces().documents(workspace.name())).remove(file);
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
			Workspace workspace = Json.fromJson(Files.readString(definition.toPath()), Workspace.class);
			return workspace.documentRoot(archetype.workspaces().documents(name).toURI());
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}
}
