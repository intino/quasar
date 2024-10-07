package io.intino.builderservice.konos.runner;

import java.io.File;

public record ProjectDirectory(File root) {


	public File res() {
		return new File(root.getAbsolutePath(), "res");
	}

	public File src() {
		return new File(root.getAbsolutePath(), "src");
	}

	public File out() {
		return new File(root.getAbsolutePath(), "out");
	}

	public File gen() {
		return new File(root.getAbsolutePath(), "gen");
	}
}
