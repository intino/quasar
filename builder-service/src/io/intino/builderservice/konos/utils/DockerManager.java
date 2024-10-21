package io.intino.builderservice.konos.utils;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.core.DockerClientBuilder;
import io.intino.alexandria.exceptions.Conflict;
import io.intino.alexandria.logger.Logger;
import io.intino.builderservice.konos.schemas.BuilderInfo;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DockerManager {


	public static void download(String imageURL) throws InterruptedException, IOException {
		try (DockerClient dockerClient = DockerClientBuilder.getInstance().build()) {
			dockerClient.pullImageCmd(imageURL).exec(new PullImageResultCallback()).awaitCompletion();
		}
	}

	public static void download(String imageURL, String registryToken) throws InterruptedException, IOException {
		try (DockerClient dockerClient = DockerClientBuilder.getInstance().build()) {
			dockerClient.authConfig().withRegistrytoken(registryToken);
			dockerClient.pullImageCmd(imageURL).exec(new PullImageResultCallback()).awaitCompletion();
		}
	}

	public static BuilderInfo builderInfo(String imageURL) throws Conflict, IOException {
		try (DockerClient dockerClient = DockerClientBuilder.getInstance().build()) {
			InspectImageResponse imageInfo = dockerClient.inspectImageCmd(imageURL).exec();
			BuilderInfo builderInfo = new BuilderInfo();
			if (imageInfo.getConfig() == null || imageInfo.getConfig().getLabels() == null)
				throw new Conflict("Configuration not found");
			Map<String, String> labels = imageInfo.getConfig().getLabels();
			if (!labels.containsKey("target")) throw new Conflict("No target label found");
			if (!labels.containsKey("operations")) throw new Conflict("No operations label found");
			labels.remove("target");
			labels.remove("operations");
			var targets = labels.get("target").split(",");
			var operations = labels.get("target").split(",");
			return builderInfo
					.imageURL(imageURL)
					.creationDate(imageInfo.getCreated())
					.targetLanguages(List.of(targets))
					.operations(List.of(operations))
					.properties(labels);
		}
	}
}