package io.intino.builderservice.konos;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.core.DockerClientBuilder;
import com.google.gson.reflect.TypeToken;
import io.intino.alexandria.Json;
import io.intino.alexandria.logger.Logger;
import io.intino.builderservice.konos.schemas.BuilderInfo;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class BuilderStore {
	private final File directory;
	private final File indexFile;
	private final Map<String, BuilderInfo> index;

	public BuilderStore(File directory) {
		this.directory = directory;
		this.indexFile = new File(directory, "builders.json");
		this.directory.mkdirs();
		this.index = load(indexFile);
	}

	private Map<String, BuilderInfo> load(File index) {
		try {
			return Json.fromJson(new FileReader(index), new TypeToken<Map<String, BuilderInfo>>() {
			}.getType());
		} catch (FileNotFoundException e) {
			Logger.error(e);
			return new HashMap<>();
		}
	}

	public void put(BuilderInfo info) {
		try {
			this.index.put(info.id(), info);
			download(info);
			saveIndex();
		} catch (InterruptedException | IOException e) {
			Logger.error(e);
		}
	}

	private void download(BuilderInfo info) throws InterruptedException, IOException {
		DockerClient dockerClient = DockerClientBuilder.getInstance().build();
		dockerClient.pullImageCmd(info.imageName()).exec(new PullImageResultCallback()).awaitCompletion();
	}

	public synchronized void saveIndex() {
		try {
			Files.writeString(indexFile.toPath(), Json.toJson(indexFile));
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public BuilderInfo get(String builderId) {
		return index.get(builderId);
	}
}
