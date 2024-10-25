import io.quassar.QuassarBuilderRunner;
import org.junit.Ignore;
import org.junit.Test;

public class BuilderRunnerUseCaseTest {

	@Test
	@Ignore
	public void should_run_builder() {
		QuassarBuilderRunner runner = new QuassarBuilderRunner();
		runner.register(DummyModelOperation.class);
		runner.start(new String[]{"test-res/tafat-m3.txt"});
	}
}
