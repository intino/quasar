import io.intino.alexandria.Resource;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.alexandria.exceptions.NotFound;
import io.intino.builderservice.QuassarBuilderServiceAccessor;
import io.intino.builderservice.konos.BuilderServiceBox;
import io.intino.builderservice.schemas.BuilderInfo;
import io.intino.builderservice.schemas.OperationResult;
import io.intino.builderservice.schemas.RegisterBuilder;
import io.intino.builderservice.schemas.RunOperationContext;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;

@Ignore
public class ApiTest {
	public static final String URL = "http://localhost:9002/";
	private static File file;

	@BeforeClass
	public static void beforeClass() throws Exception {
		file = new File("./test-res/quassar10906546769131108430.tar");
//		localQuassar();
	}

	private static void localQuassar() throws IOException {
		BuilderServiceBox box = new BuilderServiceBox(new String[]{"home=../temp",
				"language-repository=/Users/oroncal/.m2/",
				"port=9000",
				"dockerhub-auth-file=../temp/configuration/dockerhub.properties"});
		FileUtils.deleteDirectory(box.workspace());
		box.workspace().mkdirs();
		box.start();
	}


	@Test
	public void should_register_builder() throws IOException, InternalServerError, URISyntaxException {
		QuassarBuilderServiceAccessor accessor = new QuassarBuilderServiceAccessor(new URI(URL).toURL());
		accessor.postBuilders(new RegisterBuilder().imageURL("quassar625/io.quassar.quassar-builder:1.0.0"));
		List<BuilderInfo> builders = accessor.getBuilders();
		for (BuilderInfo builder : builders) {
			System.out.println(builder.imageURL() + "; " + builder.creationDate());
		}
	}

	@Test
	public void should_run_builder_service_using_accessor() throws IOException, InternalServerError, URISyntaxException, InterruptedException, NotFound {
		QuassarBuilderServiceAccessor accessor = new QuassarBuilderServiceAccessor(new URI(URL).toURL());
		String ticket = accessor.postRunOperation(context(), Resource.InputStreamProvider.of(file));
		while (accessor.getOperationOutput(ticket).state() == OperationResult.State.Running) {
			Thread.sleep(1000);
		}
		Resource out = accessor.getOutputResource(ticket, "out", ".*\\.meta$");
		Files.write(new File("test-res/out.tar").toPath(), out.inputStream().readAllBytes());
	}

	private static RunOperationContext context() {
		return new RunOperationContext()
				.operation("Build")
				.language("Meta")
				.languageVersion("2.0.0")
				.project("konos")
				.projectVersion("13.0.1")
				.generationPackage("model")
				.imageURL("quassar625/io.quassar.quassar-builder:1.0.0");
	}
}
