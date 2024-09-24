package io.intino.builderservice.konos.actions;


import io.intino.builderservice.konos.schemas.RunOperationContext;

import java.io.File;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;

import static io.intino.builder.BuildConstants.*;

public class RunConfigurationRenderer {
	private static final char NL = '\n';
	private final RunOperationContext params;
	private final File projectDir;
	private final List<String> srcFiles;
	private final File languagesRepository;

	public RunConfigurationRenderer(RunOperationContext params, File projectDir, List<String> srcFiles, File languagesRepository) {
		this.params = params;
		this.projectDir = projectDir;
		this.srcFiles = srcFiles;
		this.languagesRepository = languagesRepository;
	}

	public String build() {
		StringWriter writer = new StringWriter();
		writer.write(SRC_FILE + NL);
		for (String file : srcFiles)
			writer.write(file + "#" + true + NL);
		writer.write(NL);
		writer.write(PROJECT + NL + params.project() + NL);
		writer.write(MODULE + NL + params.project() + NL);
		writePaths(writer);
		writer.write(MAKE + NL + true + NL);
		writer.write(TEST + NL + false + NL);
		writer.write(ENCODING + NL + Charset.defaultCharset().name() + NL);
		writer.write(COMPILATION_MODE + NL + params.operation() + NL);
		if (!params.version().isEmpty()) writer.write(VERSION + NL + params.version() + NL);
		writer.write(DSL + NL + params.language() + ":" + params.languageVersion() + NL);
		writer.write(OUT_DSL + NL + params.project() + NL);
		return writer.toString();
	}

	private void writePaths(StringWriter writer) {
		writer.write(PROJECT_PATH + NL + projectDir.getAbsolutePath() + NL);
		writer.write(MODULE_PATH + NL + projectDir.getAbsolutePath() + NL);
		writer.write(OUTPUTPATH + NL + new File(projectDir.getAbsolutePath(), "gen") + NL);
		writer.write(FINAL_OUTPUTPATH + NL + new File(projectDir.getAbsolutePath(), "out") + NL);
		writer.write(RES_PATH + NL + new File(projectDir.getAbsolutePath(), "res") + NL);
		writer.write(REPOSITORY_PATH + NL + languagesRepository.getPath() + NL);
		writer.write(SRC_PATH + NL);
		writer.write(new File(projectDir.getAbsolutePath(), "src").getAbsolutePath());
		writer.write(NL);
		writer.write(NL);
	}

}

