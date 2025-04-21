import io.intino.tara.builder.core.CompilationUnit;
import io.intino.tara.builder.core.errorcollection.CompilationFailedException;
import io.intino.tara.builder.core.operation.model.ModelOperation;
import io.intino.tara.builder.model.Model;
import io.intino.tara.language.model.Mogram;

public class DummyModelOperation extends ModelOperation {

	public DummyModelOperation(CompilationUnit unit) {
		super(unit);
	}

	@Override
	public void call(Model model) throws CompilationFailedException {
		for (Mogram mogram : model.components()) {
			System.out.println(mogram.name() + "; ");
		}
	}
}
