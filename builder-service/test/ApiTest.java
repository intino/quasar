import io.intino.alexandria.Resource;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.alexandria.exceptions.NotFound;
import io.intino.builderservice.QuassarBuilderServiceAccessor;
import io.intino.builderservice.konos.BuilderServiceBox;
import io.intino.builderservice.schemas.OperationResult;
import io.intino.builderservice.schemas.RunOperationContext;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;

public class ApiTest {
	private static File file;

	@BeforeClass
	public static void beforeClass() throws Exception {
		file = new File("./test-res/quassar10906546769131108430.tar");
		BuilderServiceBox box = new BuilderServiceBox(new String[]{"home=../temp",
				"language-repository=/Users/oroncal/.m2/",
				"port=9000",
				"dockerhub-auth-file=../temp/configuration/dockerhub.properties"});
		FileUtils.deleteDirectory(box.workspace());
		box.workspace().mkdirs();
		box.start();
	}

	@Test
	public void should_run_builder_service_using_accessor() throws IOException, InternalServerError, URISyntaxException, InterruptedException, NotFound {
		QuassarBuilderServiceAccessor accessor = new QuassarBuilderServiceAccessor(new URI("http://localhost:9000/").toURL());
		String tiket = accessor.postRunOperation(context(), Resource.InputStreamProvider.of(file));
		while (accessor.getOperationOutput(tiket).state() == OperationResult.State.Running) {
			Thread.sleep(1000);
		}
		Resource out = accessor.getOutputResource(tiket, "out",  ".*\\.meta$");
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
