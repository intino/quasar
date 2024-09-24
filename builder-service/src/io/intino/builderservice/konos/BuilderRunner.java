package io.intino.builderservice.konos;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DockerClientBuilder;
import io.intino.builderservice.konos.actions.RunConfigurationRenderer;
import io.intino.builderservice.konos.schemas.BuilderInfo;
import io.intino.builderservice.konos.schemas.RunOperationContext;
import io.intino.builderservice.konos.utils.TarExtractor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

public class BuilderRunner {
	public static final String M2_BIND = "/root/.m2";
	public static final String PROJECT_BIND = "/root/project";
	private final BuilderStore store;
	private final File languagesRepository;

	public BuilderRunner(BuilderStore store, File languagesRepository) {
		this.store = store;
		this.languagesRepository = languagesRepository;
	}

	public void run(RunOperationContext params, File tarSources) throws IOException {
		BuilderInfo info = store.get(params.builderId());
		DockerClient dockerClient = DockerClientBuilder.getInstance().build();
		File projectDir = Files.createTempDirectory(params.project()).toFile();
		List<File> srcFiles = moveFiles(tarSources, projectDir);
		List<String> srcPaths = mapPaths(srcFiles, projectDir);
		RunConfigurationRenderer renderer = new RunConfigurationRenderer(params, projectDir, srcPaths, new File(M2_BIND));
		Files.writeString(new File(projectDir, "tara_args.txt").toPath(), renderer.build());
		CreateContainerResponse container = dockerClient.createContainerCmd(info.imageName())
				.withBinds(new Bind(projectDir.getAbsolutePath(), new Volume(PROJECT_BIND)),
						new Bind(languagesRepository.getAbsolutePath(), new Volume(M2_BIND)))
				.withName(params.builderId() + "_" + params.project() + "_" + UUID.randomUUID())
				.exec();
		dockerClient.startContainerCmd(container.getId()).exec();
	}

	private List<String> mapPaths(List<File> srcFiles, File projectDir) {
		return srcFiles.stream().map(f -> f.getAbsolutePath().replace(projectDir.getAbsolutePath(), PROJECT_BIND)).toList();
	}

	private static List<File> moveFiles(File tar, File projectDir) throws IOException {
		File src = new File(projectDir, "src");
		src.mkdirs();
		return TarExtractor.extractTar(tar, src);
	}
}