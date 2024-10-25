import io.intino.tara.builder.core.CompilationUnit;
import io.intino.tara.builder.core.errorcollection.CompilationFailedException;
import io.intino.tara.builder.core.operation.model.ModelOperation;
import io.intino.tara.model.Mogram;
import io.intino.tara.processors.model.Model;

public class DummyModelOperation extends ModelOperation {

	public DummyModelOperation(CompilationUnit unit) {
		super(unit);
	}

	@Override
	public void call(Model model) throws CompilationFailedException {
		for (Mogram mogram : model.mograms()) {
			System.out.println(mogram.name() + "; ");
		}
	}
}
