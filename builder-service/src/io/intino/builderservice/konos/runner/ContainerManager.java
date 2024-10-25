package io.intino.builderservice.konos.runner;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.core.DockerClientBuilder;
import io.intino.alexandria.logger.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ContainerManager {
	private final Properties properties;
	private final DockerClient client;
	private final Map<String, String> containerIds = new HashMap<>();

	public ContainerManager(File hubAuth) {
		properties = new Properties();
		load(hubAuth);
		client = DockerClientBuilder.getInstance().build();
		client.authConfig().withUsername(properties.getProperty("user")).withRegistrytoken("password");
	}

	public String createContainer(String imageName, String containerName, Bind... bind) throws IOException {
		CreateContainerResponse container = null;
		try {
			container = client.createContainerCmd(imageName)
					.withName(containerName)
					.withBinds(bind)
					.exec();
		} catch (RuntimeException e) {
			throw new IOException(e.getMessage() + ". Is docker started?");
		}
		containerIds.put(containerName, container.getId());
		return container.getId();
	}

	public void start(String container) {
		client.startContainerCmd(container).exec();
	}

	public Boolean isRunning(String ticket) {
		String containerId = containerIds.get(ticket);
		if (containerId == null) return false;
		InspectContainerResponse containerInfo = client.inspectContainerCmd(containerId).exec();
		return containerInfo.getState().getRunning();
	}

	private void load(File hubAuth) {
		try {
			properties.load(new FileInputStream(hubAuth));
		} catch (IOException e) {
			Logger.error(e);
		}
	}
}