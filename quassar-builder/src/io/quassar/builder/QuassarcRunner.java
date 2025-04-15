package io.quassar.builder;

import io.quassar.QuassarBuilderRunner;

public class QuassarcRunner {
	public static void main(String[] args) {
		QuassarBuilderRunner runner = new QuassarBuilderRunner();
		runner.register(GenerateGraphOperation.class);
		runner.register(GenerateModelReaderOperation.class);
		runner.start(args);
	}
}
