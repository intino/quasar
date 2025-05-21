import io.intino.alexandria.Json;
import io.intino.alexandria.Resource;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.alexandria.exceptions.NotFound;
import io.intino.builderservice.konos.BuilderServiceBox;
import io.intino.builderservice.konos.actions.*;
import io.intino.builderservice.konos.rest.resources.GetOutputResourceResource;
import io.intino.builderservice.konos.schemas.BuilderInfo;
import io.intino.builderservice.konos.schemas.RegisterBuilder;
import io.intino.builderservice.konos.schemas.RunOperationContext;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;

@Ignore
public class BuilderTest {

	public static final String TEMP = "../temp";
	public static final String TEST_RES = "test-res/";
	private BuilderServiceBox box;
	private String langRepository = System.getProperty("user.home") + "/.m2/";

	@Before
	public void setUp() throws IOException {
		box = new BuilderServiceBox(new String[]{"home=" + TEMP,
				"language-repository=" + langRepository,
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
		action.info = new RegisterBuilder()
				.imageURL("quassar625/io.quassar.quassar-builder:1.0.0");
		action.execute();
	}

	@Test
	public void should_retrieve_builders() {
		GetBuildersAction action = new GetBuildersAction();
		action.box = box;
		System.out.println(action.execute().stream().map(BuilderInfo::imageURL).collect(Collectors.joining("\n")));
	}

	@Test
	@Ignore
	public void should_run_build() throws InternalServerError, InterruptedException, NotFound, IOException {
		var action = new PostRunOperationAction();
		action.box = box;
		action.filesInTar = new Resource(new File(TEST_RES + "picota.tar"));
		action.runOperationContext = new RunOperationContext()
				.operation("Build")
				.languageGroup("tara.dsl")
				.languageName("metta")
				.languageVersion("2.0.0")
				.languagePath(langRepository  + "repository/tara/dsl/metta/2.0.0/metta-2.0.0.jar")
				.projectGroup("io.intino")
				.projectName("konos")
				.projectVersion("13.0.1")
				.generationPackage("model")
				.imageURL("quassar625/io.quassar.quassar-builder:1.0.0");
		String ticket = action.execute();
		Thread.sleep(10000);
		GetOperationOutputAction get = new GetOperationOutputAction();
		get.box = box;
		get.ticket = ticket;
		System.out.println(Json.toJsonPretty(get.execute()));
		var output = new GetOutputResourceAction();
		output.box = box;
		output.ticket = ticket;
		output.output = GetOutputResourceResource.Output.Build;
		Resource execute = output.execute();
		Files.write(new File(TEST_RES + "build.tar").toPath(), execute.inputStream().readAllBytes());
	}
}