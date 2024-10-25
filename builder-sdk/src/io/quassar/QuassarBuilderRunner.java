package io.quassar;

import io.intino.tara.builder.TaraCompilerRunner;
import io.intino.tara.builder.core.errorcollection.TaraException;
import io.intino.tara.builder.core.operation.Operation;
import io.intino.tara.builder.core.operation.model.ModelOperation;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static io.quassar.Check.checkArgumentsNumber;
import static io.quassar.Check.checkConfigurationFile;

public class QuassarBuilderRunner {
	static final PrintStream LOG = System.out;
	private final List<Class<? extends Operation>> operations = new ArrayList<>();

	public void start(String[] args) {
		try {
			if (checkArgumentsNumber(args) || (!checkConfigurationFile(args[0])))
				throw new TaraException("Error finding args file");
			new TaraCompilerRunner(true, operations).run(new File(args[0]));
		} catch (Exception e) {
			LOG.println(e.getMessage() == null ? e.getStackTrace()[0].toString() : e.getMessage());
			System.exit(1);
		}
	}

	public void register(Class<? extends ModelOperation> operation) {
		operations.add(operation);
	}
}