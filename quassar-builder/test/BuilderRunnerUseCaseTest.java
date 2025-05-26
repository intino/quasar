import io.intino.tara.builder.utils.FileSystemUtils;
import io.quassar.QuassarBuilderRunner;
import io.quassar.builder.QuassarcRunner;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

public class BuilderRunnerUseCaseTest {

	@Test
	@Ignore
	public void should_run_builder() {
		QuassarBuilderRunner runner = new QuassarBuilderRunner();
		runner.register(DummyModelOperation.class);
		runner.start(new String[]{"test-res/tafat-m3.txt"});
	}

	@Test
	@Ignore
	public void should_run_quassar_builder_with_m3() {
		QuassarcRunner.main(new String[]{"test-res/tafat-m3.txt"});
	}

	@Test
	@Ignore
	public void should_run_quassar_builder_with_m2() {
		FileSystemUtils.removeDir(new File("/Users/oroncal/workspace/sandbox/flogo/flogo-accessor"));
		QuassarcRunner.main(new String[]{"test-res/flogo-m2.txt"});
	}

	@Test
	@Ignore
	public void should_run_quassar_builder_with_picota_m2() {
		QuassarcRunner.main(new String[]{"/Users/oroncal/workspace/infrastructure/quassar/temp/builder-service/workspace/garaxia/tara_args.txt"});
	}

	@Test
	@Ignore
	public void should_run_quassar_builder_with_m2_2() {
		QuassarcRunner.main(new String[]{"test-res/visora-m2.txt"});
	}
}
