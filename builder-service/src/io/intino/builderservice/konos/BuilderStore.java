package io.intino.builderservice.konos;

import com.google.gson.reflect.TypeToken;
import io.intino.alexandria.Json;
import io.intino.alexandria.logger.Logger;
import io.intino.builderservice.konos.runner.ContainerManager;
import io.intino.builderservice.konos.schemas.BuilderInfo;
import io.intino.builderservice.konos.schemas.RegisterBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BuilderStore {
	private final File indexFile;
	private final Map<String, BuilderInfo> index;
	private final ContainerManager dockerManager;

	public BuilderStore(ContainerManager dockerManager, File directory) {
		this.dockerManager = dockerManager;
		directory.mkdirs();
		this.indexFile = new File(directory, "builders.json");
		this.index = load(indexFile);
	}

	public Collection<BuilderInfo> all() {
		return index.values();
	}

	private Map<String, BuilderInfo> load(File index) {
		try {
			if (!index.exists()) return new HashMap<>();
			return Json.fromJson(new FileReader(index), new TypeToken<HashMap<String, BuilderInfo>>() {
			}.getType());
		} catch (FileNotFoundException e) {
			Logger.error(e);
			return new HashMap<>();
		}
	}

	public void put(RegisterBuilder info) {
		try {
			dockerManager.download(info.imageURL(), info.registryToken());
			this.index.put(info.imageURL(), new BuilderInfo().imageURL(info.imageURL()));
			saveIndex();
		} catch (InterruptedException | IOException e) {
			Logger.error(e);
		}
	}

	public synchronized void saveIndex() {
		try {
			Files.writeString(indexFile.toPath(), Json.toJson(index));
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public BuilderInfo get(String builderId) {
		return index.get(builderId);
	}
}