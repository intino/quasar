package io.quassar.editor.model;

public class WorkspaceProperties {
	private String metamodel;
	private String release;

	public WorkspaceProperties(String metamodel) {
		this.metamodel = metamodel;
	}

	public String metamodel() {
		return metamodel;
	}

	public WorkspaceProperties metamodel(String metamodel) {
		this.metamodel = metamodel;
		return this;
	}

	public String release() {
		return release;
	}

	public WorkspaceProperties release(String release) {
		this.release = release;
		return this;
	}
}
