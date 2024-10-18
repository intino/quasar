import io.intino.ime.box.orchestator.BuilderOrchestator;
import io.intino.ime.box.orchestator.ProjectCreator;
import io.intino.ime.box.scaffolds.IntellijScaffold;
import io.intino.ls.document.FileDocumentManager;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static io.intino.ime.box.scaffolds.ScaffoldFactory.Scaffold.Intellij;

public class ImeTest {

	public static void main(String[] args) throws IOException, URISyntaxException {
		//testCreateProject();
		testBuilderService();
		//testScaffold();
	}

	private static void testCreateProject() throws IOException {
		new ProjectCreator("Tafat:1.0.0", "Meta:2.0.0", "io.tafat",
				List.of(new ProjectCreator.CodeBucket("code/java/tafat", Intellij, "io.intino.magritte.builder:1.3.0")))
				.create(new FileDocumentManager(new File("temp/projects/tafat")));
	}

	private static void testScaffold() throws IOException {
		final FileDocumentManager fileDocumentManager = new FileDocumentManager(new File("temp/projects/tafat"));
		new IntellijScaffold(fileDocumentManager, "code/java/Tafat").build();
	}

	private static void testBuilderService() throws IOException, URISyntaxException {
		final FileDocumentManager fileDocumentManager = new FileDocumentManager(new File("temp/projects/tafat"));
		new BuilderOrchestator(new URI("http://localhost:9000").toURL(), fileDocumentManager).build("");
	}


	private static void checkAPI() {
		String url = "http://localhost:9000/api/operations/run";
		String jsonParam = "{\"builderId\":\"io.intino.tara:1.3.0\",\"operation\":\"Build\",\"project\":\"Tafat\",\"projectVersion\":\"1.0.0\",\"language\":\"Meta\",\"languageVersion\":\"2.0.0\",\"generationPackage\":\"\"}"; // Replace with your JSON content
		File file = new File("/private/var/folders/l9/1t_5_g7d0hx665k00231gy0h0000gn/T/quassar10906546769131108430.tar"); // Replace with your file path

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
}
