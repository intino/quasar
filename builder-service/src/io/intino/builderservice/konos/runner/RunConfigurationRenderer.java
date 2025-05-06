package io.intino.builderservice.konos.runner;

import io.intino.builder.CompilerConfiguration;
import io.intino.builderservice.konos.schemas.RunOperationContext;

import java.io.File;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;

import static io.intino.builder.BuildConstants.*;

public class RunConfigurationRenderer {
	private static final char NL = '\n';
	private final RunOperationContext params;
	private final ProjectDirectory project;
	private final List<String> srcFiles;
	private final File languagesRepository;

	public RunConfigurationRenderer(RunOperationContext params, ProjectDirectory project, List<String> srcFiles, File languagesRepository) {
		this.params = params;
		this.project = project;
		this.srcFiles = srcFiles;
		this.languagesRepository = languagesRepository;
	}

	public String build() {
		StringWriter writer = new StringWriter();
		writer.write(SRC_FILE + NL);
		srcFiles.stream().map(file -> file + "#" + true + NL).forEach(writer::write);
		writer.write(NL);
		writer.write(PROJECT + NL + params.project() + NL);
		writer.write(MODULE + NL + params.project() + NL);
		writer.write(ARTIFACT_ID + NL + params.project() + NL);
		writePaths(writer);
		writer.write(MAKE + NL + true + NL);
		writer.write(TEST + NL + false + NL);
		writer.write(ENCODING + NL + Charset.defaultCharset().name() + NL);
		if (params.operation().equalsIgnoreCase("check")) writer.write(EXCLUDED_PHASES + NL + "8,9" + NL);
		writer.write(GENERATION_PACKAGE + NL + (params.generationPackage().isEmpty() ? "model" : params.generationPackage()) + NL);
		writer.write(COMPILATION_MODE + NL + params.operation() + NL);
		if (!params.projectVersion().isEmpty()) writer.write(VERSION + NL + params.projectVersion() + NL);
		writer.write(DSL + NL + params.language() + ":" + params.languageVersion() + NL);
		writer.write(OUT_DSL + NL + params.project() + NL);
		return writer.toString();
	}

	private void writePaths(StringWriter writer) {
		writer.write(PROJECT_PATH + NL + project.root().getAbsolutePath() + NL);
		writer.write(MODULE_PATH + NL + project.root().getAbsolutePath() + NL);
		writer.write(OUTPUTPATH + NL + project.gen().getAbsolutePath() + NL);
		writer.write(FINAL_OUTPUTPATH + NL + project.out().getAbsolutePath() + NL);
		writer.write(RES_PATH + NL + project.res() + NL);
		writer.write(REPOSITORY_PATH + NL + languagesRepository.getPath() + NL);
		writer.write(SRC_PATH + NL);
		writer.write(project.src().getAbsolutePath());
		writer.write(NL);
		writer.write(NL);
	}
}