package io.intino.builderservice.konos.runner;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import io.intino.alexandria.exceptions.Conflict;
import io.intino.alexandria.logger.Logger;
import io.intino.builderservice.konos.schemas.BuilderInfo;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ContainerManager {
	private final Properties properties;
	private final Map<String, String> containerIds = new HashMap<>();
	private final String url;
	private final String hostUser;

	public ContainerManager(String url, File hubAuth) {
		this.url = url;
		properties = new Properties();
		load(hubAuth);
		hostUser = hostUser();
	}

	public void download(String imageURL) throws InterruptedException, IOException {
		try (DockerClient dockerClient = dockerClient()) {
			dockerClient.pullImageCmd(imageURL).exec(new PullImageResultCallback()).awaitCompletion();
		}
	}

	public void download(String imageURL, String registryToken) throws InterruptedException, IOException {
		try (DockerClient dockerClient = dockerClient()) {
			if (registryToken != null) dockerClient.authConfig().withRegistrytoken(registryToken);
			dockerClient.pullImageCmd(imageURL).exec(new PullImageResultCallback()).awaitCompletion();
		}
	}

	public BuilderInfo builderInfo(String imageURL) throws Conflict, IOException {
		try (DockerClient dockerClient = dockerClient()) {
			InspectImageResponse imageInfo = dockerClient.inspectImageCmd(imageURL).exec();
			BuilderInfo builderInfo = new BuilderInfo();
			if (imageInfo.getConfig() == null || imageInfo.getConfig().getLabels() == null)
				throw new Conflict("Configuration not found");
			Map<String, String> labels = imageInfo.getConfig().getLabels();
			if (!labels.containsKey("targets")) throw new Conflict("No target label found");
			if (!labels.containsKey("operations")) throw new Conflict("No operations label found");
			var targets = labels.get("targets").split(",");
			var operations = labels.get("operations").split(",");
			labels.remove("targets");
			labels.remove("operations");
			return builderInfo
					.imageURL(imageURL)
					.creationDate(imageInfo.getCreated())
					.targetLanguages(List.of(targets))
					.operations(List.of(operations))
					.properties(labels);
		}
	}

	private DockerClient dockerClient() {
		DockerClient client = url != null ? DockerClientBuilder.getInstance(url).build() : DockerClientBuilder.getInstance().build();
		client.authConfig().withUsername(properties.getProperty("user")).withRegistrytoken("password");
		return client;
	}

	public String createContainer(String imageName, String containerName, Bind... bind) throws IOException {
		try (DockerClient client = dockerClient()) {
			CreateContainerResponse container = client.createContainerCmd(imageName)
					.withName(containerName)
					.withUser(hostUser)
					.withHostConfig(HostConfig.newHostConfig().withBinds(bind).withAutoRemove(true))
					.exec();
			containerIds.put(containerName, container.getId());
			return container.getId();
		} catch (Exception e) {
			throw new IOException(e.getMessage() + ". Is docker started?");
		}
	}

	public void start(String container) {
		try (var client = dockerClient()) {
			client.startContainerCmd(container).exec();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Boolean isRunning(String ticket) {
		String containerId = containerIds.get(ticket);
		if (containerId == null) return false;
		try (var client = dockerClient()) {
			if (client.listContainersCmd().exec().stream().noneMatch(c -> c.getId().equalsIgnoreCase(containerId)))
				return false;
			InspectContainerResponse containerInfo = client.inspectContainerCmd(containerId).exec();
			return containerInfo.getState().getRunning();
		} catch (IOException e) {
			return false;
		}
	}

	private void load(File hubAuth) {
		try {
			properties.load(new FileInputStream(hubAuth));
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private String hostUser() {
		try {
			Process uidProcess = new ProcessBuilder("id", "-u").start();
			Process gidProcess = new ProcessBuilder("id", "-g").start();
			BufferedReader uidReader = new BufferedReader(new InputStreamReader(uidProcess.getInputStream()));
			BufferedReader gidReader = new BufferedReader(new InputStreamReader(gidProcess.getInputStream()));
			return uidReader.readLine().trim() + ":" + gidReader.readLine().trim();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}
}