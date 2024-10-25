package modelaccessor;

import io.intino.builder.CompilerConfiguration;
import io.intino.itrules.Engine;
import io.intino.itrules.FrameBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ModelAccessorGenerator {

	private final CompilerConfiguration conf;

	public ModelAccessorGenerator(CompilerConfiguration conf) {
		this.conf = conf;
	}

	public void generate() {
		generateModelAccessorClass();
		File pom = generatePom();
		if (pom != null) new MavenExecutor().run(pom, new File(conf.outDirectory(), "mvn.log"));
	}

	private void generateModelAccessorClass() {
		try {
			FrameBuilder builder = new FrameBuilder().add("modelaccessor")
					.add("outdsl", conf.dsl().outDsl())
					.add("version", conf.version())
					.add("package", conf.generationPackage());
			String content = new Engine(new ModelAccessorTemplate()).render(builder.toFrame());
			File dir = new File(conf.srcDirectory(), conf.generationPackage().replace(".", "/"));
			dir.mkdirs();
			Files.writeString(new File(dir, "ModelAccessor.java").toPath(), content);
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
