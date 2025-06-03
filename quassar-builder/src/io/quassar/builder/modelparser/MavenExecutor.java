package io.quassar.builder.modelparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class MavenExecutor {

	public boolean run(File pom, File localRepository, File log) {
		try {
			String mvnCommand = "mvn";
			List<String> command = new ArrayList<>();
			command.add(mvnCommand);
			command.add("-Dmaven.repo.local=" + localRepository.getAbsolutePath());
			command.add("install");
			ProcessBuilder processBuilder = new ProcessBuilder(command);
			processBuilder.directory(pom.getParentFile());
			processBuilder.redirectErrorStream(true);
			Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			try (PrintWriter writer = new PrintWriter(log)) {
				while ((line = reader.readLine()) != null) {
					writer.println(line);
				}
				int exitCode = process.waitFor();
				writer.println("mvn finished with exit code: " + exitCode);
			}
			return process.exitValue() == 0;
		} catch (Exception ignored) {
			ignored.printStackTrace();
		}
		return false;
	}
}