import io.intino.builderservice.konos.BuilderServiceBox;
import io.intino.builderservice.konos.actions.PostBuildersAction;
import io.intino.builderservice.konos.schemas.BuilderInfo;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.util.List;
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

public class MainTest {

	public static void main(String[] args) throws IOException {
		BuilderServiceBox box = new BuilderServiceBox(new String[]{"home=temp",
				"language-repository=/Users/jevora/.m2/",
				"port=9000",
				"dockerhub-auth-file=temp/configuration/dockerhub.properties"});
		FileUtils.deleteDirectory(box.workspace());
		box.workspace().mkdirs();
		box.start();

		var action = new PostBuildersAction();
		action.box = box;
		action.builderInfo = new BuilderInfo()
				.id("io.intino.tara.builder:1.3.0")
				.imageName("octavioroncal/io.intino.tara.builder:1.3.0")
				.operations(List.of("Build"));
		action.execute();
		System.out.println("DONE");
	}

}
