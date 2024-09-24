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
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class BuilderStore {
	private final File directory;
	private final File indexFile;
	private final File images;
	private final Map<String, BuilderInfo> index;

	public BuilderStore(File directory) {
		this.directory = directory;
		this.indexFile = new File(directory, "builders.json");
		this.images = new File(directory, "images");
		this.directory.mkdirs();
		this.images.mkdirs();
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
		String outputFilePath = new File(images, info.imageName().replace(":", "-") + ".tar").getAbsolutePath();
		DockerClient dockerClient = DockerClientBuilder.getInstance().build();
		dockerClient.pullImageCmd(info.imageName()).exec(new PullImageResultCallback()).awaitCompletion();
		try (InputStream inputStream = dockerClient.saveImageCmd(info.imageName()).exec();
			 OutputStream outputStream = new FileOutputStream(Paths.get(outputFilePath).toFile())) {
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
		}
	}

	public synchronized void saveIndex() {
		try {
			Files.writeString(indexFile.toPath(), Json.toJson(indexFile));
		} catch (IOException e) {
			Logger.error(e);
		}
	}
}
