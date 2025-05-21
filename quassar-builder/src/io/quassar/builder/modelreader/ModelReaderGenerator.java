package io.quassar.builder.modelreader;

import io.intino.builder.CompilerConfiguration;
import io.intino.itrules.Engine;
import io.intino.itrules.FrameBuilder;
import io.intino.tara.builder.utils.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ModelReaderGenerator {
	private final CompilerConfiguration conf;

	public ModelReaderGenerator(CompilerConfiguration conf) {
		this.conf = conf;
	}

	public boolean generate() {
		generateModelReaderClass();
		File pom = generatePom();
		if (pom != null) {
			boolean result = new MavenExecutor().run(pom, conf.localRepository(), new File(conf.moduleDirectory(), "mvn.log"));
			File build = new File(pom.getParentFile(), "out/build/" + conf.module() + "-model-reader-java");
			FileSystemUtils.removeDir(new File(build, "maven-archiver"));
			FileSystemUtils.removeDir(new File(build, "maven-status"));
			FileSystemUtils.removeDir(new File(build, "generated-sources"));
			return result;
		}
		return false;
	}

	private void generateModelReaderClass() {
		try {
			FrameBuilder builder = new FrameBuilder().add("modelreader")
					.add("outdsl", conf.dsl().outDsl())
					.add("outdslCoors", conf.groupId() + ":" + conf.dsl().outDsl() + ":" + conf.version())
					.add("version", conf.version())
					.add("package", conf.generationPackage());
			String content = new Engine(new ModelReaderTemplate()).render(builder.toFrame());
			File dir = new File(conf.srcDirectory(), conf.generationPackage().replace(".", "/"));
			dir.mkdirs();
			Files.writeString(new File(dir, "ModelReader.java").toPath(), content);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	private File generatePom() {
		try {
			FrameBuilder builder = new FrameBuilder("pom")
					.add("groupId", conf.groupId() == null ? "io.quassar" : conf.groupId())
					.add("name", conf.artifactId())
					.add("version", conf.version());
			String content = new Engine(new POMTemplate()).render(builder.toFrame());
			File dir = conf.moduleDirectory();
			dir.mkdirs();
			File pomFile = new File(dir, "pom.xml");
			Files.writeString(pomFile.toPath(), content);
			return pomFile;
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return null;
		}
	}
}