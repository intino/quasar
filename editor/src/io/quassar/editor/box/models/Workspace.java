package io.quassar.editor.box.models;

import io.quassar.archetype.Archetype;
import io.quassar.editor.box.util.WorkspaceHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.net.URI;

public class Workspace {
	private final Language language;
	private final Model model;
	private final String release;
	private final Archetype archetype;

	public Workspace(Language language, Model model, String release, Archetype archetype) {
		this.language = language;
		this.model = model;
		this.release = release;
		this.archetype = archetype;
	}

	public java.io.File root() {
		URI workspace = WorkspaceHelper.workspace(model, release, archetype);
		return workspace != null ? new java.io.File(workspace) : null;
	}

	public Language language() {
		return language;
	}

	public Model model() {
		return model;
	}

	public String release() {
		return release;
	}

	public Workspace clone(Model destinyModel) {
		return new Workspace(language, destinyModel, release, archetype);
	}
}
