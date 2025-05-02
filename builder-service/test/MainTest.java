import io.intino.builderservice.konos.BuilderServiceBox;
import io.intino.builderservice.konos.actions.PostBuildersAction;
import io.intino.builderservice.konos.schemas.RegisterBuilder;
import org.apache.commons.io.FileUtils;
import org.junit.Ignore;

import java.io.IOException;

@Ignore
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
		action.info = new RegisterBuilder()
				.imageURL("octavioroncal/io.intino.tara.builder:1.3.0");
		action.execute();
		System.out.println("DONE");
	}

}
