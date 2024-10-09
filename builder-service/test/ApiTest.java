import io.intino.builderservice.konos.BuilderServiceBox;
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

public class ApiTest {


	@BeforeClass
	public static void beforeClass() throws Exception {
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
		File file = new File("./test-res/quassar10906546769131108430.tar"); // Replace with your file path

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
