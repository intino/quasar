package io.intino.builderservice.konos.utils;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.core.DockerClientBuilder;
import io.intino.alexandria.exceptions.Conflict;
import io.intino.builderservice.konos.schemas.BuilderInfo;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DockerManager {
	private final String url;

	public DockerManager(String url) {
		this.url = url;
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
		return url != null ? DockerClientBuilder.getInstance(url).build() : DockerClientBuilder.getInstance().build();
	}
}