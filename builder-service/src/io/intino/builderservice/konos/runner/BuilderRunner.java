package io.intino.builderservice.konos.runner;

import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Volume;
import io.intino.builderservice.konos.BuilderStore;
import io.intino.builderservice.konos.schemas.BuilderInfo;
import io.intino.builderservice.konos.schemas.RunOperationContext;
import io.intino.builderservice.konos.utils.TarExtractor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

public class BuilderRunner {
	public static final String M2_BIND = "/root/.m2";
	public static final String PROJECT_BIND = "/root/project";
	private final BuilderStore store;
	private final ContainerManager manager;
	private final File workspace;
	private final File languagesRepository;

	public BuilderRunner(BuilderStore store, ContainerManager manager, File workspace, File languagesRepository) {
		this.store = store;
		this.manager = manager;
		this.workspace = workspace;
		this.languagesRepository = languagesRepository;
	}

	public String run(RunOperationContext params, InputStream tarSources) throws IOException {
		BuilderInfo info = store.get(params.builderId());
		String ticket = UUID.randomUUID().toString();
		File projectDir = new File(workspace, ticket);
		List<File> srcFiles = moveFiles(tarSources, projectDir);
		List<String> srcPaths = mapPaths(srcFiles, projectDir);
		RunConfigurationRenderer renderer = new RunConfigurationRenderer(params, new ProjectDirectory(new File(PROJECT_BIND)), srcPaths, new File(M2_BIND));
		Files.writeString(new File(projectDir, "tara_args.txt").toPath(), renderer.build());
		String container = manager.createContainer(info.imageName(), ticket,
				new Bind(projectDir.getCanonicalFile().getAbsolutePath(), new Volume(PROJECT_BIND)),
				new Bind(languagesRepository.getAbsolutePath(), new Volume(M2_BIND)));
		manager.start(container);
		return ticket;
	}

	private List<String> mapPaths(List<File> srcFiles, File projectDir) {
		return srcFiles.stream().map(f -> f.getAbsolutePath().replace(projectDir.getAbsolutePath(), PROJECT_BIND)).toList();
	}

	private static List<File> moveFiles(InputStream tar, File projectDir) throws IOException {
		File src = new File(projectDir, "src");
		src.mkdirs();
		return TarExtractor.extractTar(tar, src);
	}
}