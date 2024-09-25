import io.intino.builderservice.konos.BuilderServiceBox;
import io.intino.builderservice.konos.actions.PostBuildersAction;
import io.intino.builderservice.konos.schemas.BuilderInfo;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class BuilderTest {

	private BuilderServiceBox box;

	@Before
	public void setUp() {
		box = new BuilderServiceBox(new String[]{"home=../temp",
				"language-repository/Users/oroncal/.m2/",
				"port=9000",
				"dockerhub-auth-file=../temp/configuration/dockerhub-auth.json"});
		box.start();
	}

	@Test
	public void should_register_builder() {
		PostBuildersAction action = new PostBuildersAction();
		action.box = box;
		action.builderInfo = new BuilderInfo()
				.id("io.intino.tara.builder:1.3.0")
				.imageName("octavioroncal/io.intino.tara.builder:1.3.0")
				.operations(List.of("build"));
		action.execute();
	}

	@Test
	public void should_retrieve_builders() {

	}

	@Test
	public void should_run_build() {

	}
}
