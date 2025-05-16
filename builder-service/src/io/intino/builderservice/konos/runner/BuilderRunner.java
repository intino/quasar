package io.intino.builderservice.konos.runner;

import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.SELContext;
import com.github.dockerjava.api.model.Volume;
import io.intino.builderservice.konos.BuilderStore;
import io.intino.builderservice.konos.schemas.BuilderInfo;
import io.intino.builderservice.konos.schemas.RunOperationContext;
import io.intino.builderservice.konos.utils.Tar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.UUID;

import static io.intino.builderservice.konos.runner.ProjectDirectory.PROJECT_BIND;

public class BuilderRunner {
	public static final String REPOSITORY_BIND = "/app/.m2/repository";
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

	public SimpleEntry<String, List<File>> run(RunOperationContext params, InputStream tarSources) throws IOException {
		BuilderInfo info = store.get(params.imageURL());
		String ticket = UUID.randomUUID().toString();
		ProjectDirectory hostProject = ProjectDirectory.of(workspace.getCanonicalFile(), ticket);
		List<File> srcFiles = moveFiles(tarSources, hostProject.root());
		List<String> srcPaths = mapPaths(srcFiles, hostProject);
		ProjectDirectory containerProject = new ProjectDirectory(new File(PROJECT_BIND));
		RunConfigurationRenderer renderer = new RunConfigurationRenderer(params, containerProject, srcPaths, new File(REPOSITORY_BIND));
		Files.writeString(hostProject.argsFile().toPath().toAbsolutePath(), renderer.build());
		String container = manager.createContainer(info.imageURL(), ticket,
				new Bind(hostProject.root().getCanonicalFile().getAbsolutePath(), new Volume(PROJECT_BIND), AccessMode.rw, SELContext.single),
				new Bind(languagesRepository.getAbsolutePath(), new Volume(REPOSITORY_BIND), AccessMode.rw, SELContext.single));
		manager.start(container);
		return new SimpleEntry<>(ticket, srcFiles);
	}

	private List<String> mapPaths(List<File> srcFiles, ProjectDirectory projectDir) {
		return srcFiles.stream().map(File::getAbsolutePath).map(projectDir.directoryMapper()).toList();
	}

	private static List<File> moveFiles(InputStream tar, File projectDir) throws IOException {
		File src = new File(projectDir, "src");
		src.mkdirs();
		return Tar.unTar(tar, src);
	}
}