package io.intino.builderservice.konos.runner;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;

public record ProjectDirectory(File root) {

	public ProjectDirectory(File root) {
		try {
			this.root = root.getCanonicalFile();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static final String PROJECT_BIND = "/project";

	public static ProjectDirectory of(File root, String name) {
		return new ProjectDirectory(new File(root, name));
	}

	public Function<String, String> directoryMapper() {
		return f -> f.replace(root.getAbsolutePath(), PROJECT_BIND);
	}

	public Function<String, String> reverseDirectoryMapper() {
		return f -> {
			try {
				return new File(f.replace(PROJECT_BIND, root.getAbsolutePath())).getCanonicalPath();
			} catch (IOException e) {
				return f;
			}
		};
	}

	public boolean exists() {
		return root.exists();
	}

	public File argsFile() {
		return new File(root, "tara_args.txt").getAbsoluteFile();
	}

	public File logFile() {
		return new File(root, "output.log").getAbsoluteFile();
	}

	public File res() {
		return new File(root.getAbsolutePath(), "res");
	}

	public File src() {
		return new File(root.getAbsolutePath(), "src");
	}

	public File out() {
		return new File(root.getAbsolutePath(), "out" + File.separator + "production");
	}

	public File build() {
		return new File(root.getAbsolutePath(), "out"+ File.separator + "build");
	}

	public File gen() {
		return new File(root.getAbsolutePath(), "gen");
	}
}
