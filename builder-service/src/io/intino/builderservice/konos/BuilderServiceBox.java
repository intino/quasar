package io.intino.builderservice.konos;

import java.io.File;

public class BuilderServiceBox extends AbstractBox {
	private final BuilderStore builderStore;
	private final File workspace;

	public BuilderServiceBox(String[] args) {
		this(new BuilderServiceConfiguration(args));
	}

	public BuilderServiceBox(BuilderServiceConfiguration configuration) {
		super(configuration);
		this.builderStore = new BuilderStore(new File(configuration.home(), "builder-service/store"));
		this.workspace = new File(configuration.home(), "builder-service/workspace");
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

	public void beforeStart() {

	}

	public void afterStart() {

	}

	public void beforeStop() {

	}

	public void afterStop() {

	}
}