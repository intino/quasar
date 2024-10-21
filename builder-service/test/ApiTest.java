import io.intino.alexandria.Resource;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.alexandria.exceptions.NotFound;
import io.intino.builderservice.QuassarBuilderServiceAccessor;
import io.intino.builderservice.konos.BuilderServiceBox;
import io.intino.builderservice.schemas.OperationResult;
import io.intino.builderservice.schemas.RunOperationContext;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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
	public void should_run_builder_service_api() {
		String url = "http://localhost:9000/api/operations/run";
		String jsonParam = "{\"builderId\":\"io.intino.tara.builder:1.3.0\",\"operation\":\"Build\",\"project\":\"Tafat\",\"projectVersion\":\"1.0.0\",\"language\":\"Meta\",\"languageVersion\":\"2.0.0\",\"generationPackage\":\"\"}"; // Replace with your JSON content
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpPost httpPost = new HttpPost(url);

			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.addTextBody("runOperationContext", jsonParam, ContentType.APPLICATION_JSON);
			builder.addBinaryBody("filesInTar", file, ContentType.APPLICATION_OCTET_STREAM, file.getName());

			HttpEntity multipart = builder.build();
			httpPost.setEntity(multipart);

			try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
				HttpEntity responseEntity = response.getEntity();
				if (responseEntity != null) {
					String result = EntityUtils.toString(responseEntity);
					System.out.println("Response: " + result);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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
				.imageURL("octavioroncal/io.intino.tara.builder:1.3.0");
	}
}
