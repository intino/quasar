import io.intino.alexandria.Json;
import io.intino.alexandria.Resource;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.alexandria.exceptions.NotFound;
import io.intino.builderservice.konos.BuilderServiceBox;
import io.intino.builderservice.konos.actions.*;
import io.intino.builderservice.konos.schemas.BuilderInfo;
import io.intino.builderservice.konos.schemas.RunOperationContext;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

public class BuilderTest {

	public static final String TEMP = "../temp";
	public static final String TEST_RES = "test-res/";
	private BuilderServiceBox box;

	@Before
	public void setUp() throws IOException {
		box = new BuilderServiceBox(new String[]{"home=" + TEMP,
				"language-repository=" + System.getProperty("user.home") + "/.m2/",
				"port=9000",
				"dockerhub-auth-file=" + TEMP + "/configuration/dockerhub.properties"});
		FileUtils.deleteDirectory(box.workspace());
		box.workspace().mkdirs();
		box.start();
	}

	@Test
	public void should_register_builder() {
		var action = new PostBuildersAction();
		action.box = box;
		action.builderInfo = new BuilderInfo()
				.id("io.intino.tara.builder:1.3.0")
				.imageName("octavioroncal/io.intino.tara.builder:1.3.0")
				.operations(List.of("Build"));
		action.execute();
	}

	@Test
	public void should_retrieve_builders() {
		GetBuildersAction action = new GetBuildersAction();
		action.box = box;
		System.out.println(action.execute().stream().map(b -> String.join(" - ", b.id(), b.imageName())).collect(Collectors.joining("\n")));
	}

	@Test
	public void should_run_build() throws InternalServerError, InterruptedException, NotFound, IOException {
		var action = new PostRunOperationAction();
		action.box = box;
		action.filesInTar = new Resource(new File(TEST_RES + "sources.tar"));
		action.runOperationContext = new RunOperationContext()
				.operation("Build")
				.language("Meta")
				.languageVersion("2.0.0")
				.project("konos")
				.projectVersion("13.0.1")
				.generationPackage("model")
				.builderId("io.intino.tara.builder:1.3.0");
		String ticket = action.execute();
		Thread.sleep(10000);
		GetOperationOutputAction get = new GetOperationOutputAction();
		get.box = box;
		get.ticket = ticket;
		System.out.println(Json.toJsonPretty(get.execute()));
		var output = new GetOutputResourceAction();
		output.box = box;
		output.ticket = ticket;
		output.output = "out";
		Resource execute = output.execute();
		Files.write(new File(TEST_RES + "out.tar").toPath(), execute.inputStream().readAllBytes());
	}
}