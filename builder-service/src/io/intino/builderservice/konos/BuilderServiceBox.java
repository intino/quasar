package io.intino.builderservice.konos;

import io.intino.alexandria.logger.Logger;
import io.intino.builderservice.konos.runner.ContainerManager;
import io.intino.builderservice.konos.runner.OperationOutputHandler;
import io.intino.builderservice.konos.runner.ProjectDirectory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuilderServiceBox extends AbstractBox {
	private final BuilderStore builderStore;
	private final ContainerManager containerManager;
	private final File workspace;
	private final Map<String, OperationOutputHandler> operationHandlers;

	public BuilderServiceBox(String[] args) {
		this(new BuilderServiceConfiguration(args));
	}

	public BuilderServiceBox(BuilderServiceConfiguration configuration) {
		super(configuration);
		this.containerManager = new ContainerManager(configuration.dockerUrl(), configuration().dockerhubAuthFile());
		this.builderStore = new BuilderStore(containerManager, new File(configuration.home(), "builder-service/store"));
		this.workspace = new File(configuration.home(), "builder-service/workspace");
		this.operationHandlers = new HashMap<>();
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

	public void registerOperationHandler(String ticket, List<File> srcFiles) {
		if (this.operationHandlers.containsKey(ticket)) Logger.error("Ticket " + ticket + " already registered");
		else {
			ProjectDirectory projectDirectory = ProjectDirectory.of(workspace(), ticket);
			this.operationHandlers.put(ticket, new OperationOutputHandler(projectDirectory, srcFiles));
		}
	}

	public OperationOutputHandler operationHandler(String ticket) {
		return operationHandlers.get(ticket);
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