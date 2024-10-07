package io.intino.builderservice.konos;

import io.intino.builderservice.konos.runner.ContainerManager;

import java.io.File;

public class BuilderServiceBox extends AbstractBox {
	private final BuilderStore builderStore;
	private final ContainerManager containerManager;
	private final File workspace;

	public BuilderServiceBox(String[] args) {
		this(new BuilderServiceConfiguration(args));
	}

	public BuilderServiceBox(BuilderServiceConfiguration configuration) {
		super(configuration);
		this.builderStore = new BuilderStore(new File(configuration.home(), "builder-service/store"));
		this.workspace = new File(configuration.home(), "builder-service/workspace");
		this.containerManager = new ContainerManager(configuration().dockerhubAuthFile());
		this.workspace.mkdirs();
	}

	@Override
	public io.intino.alexandria.core.Box put(Object o) {
		super.put(o);
		return this;
	}

	public BuilderStore builderStore() {
		return builderStore;
	}

	public File workspace() {
		return workspace;
	}

	public ContainerManager containerManager() {
		return containerManager;
	}

	public void beforeStart() {

	}

	public void afterStart() {

	}

	public void beforeStop() {

	}

	public void afterStop() {

	}
}