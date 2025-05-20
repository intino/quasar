package io.quassar.builder.modelreader;

import io.intino.itrules.template.Rule;
import io.intino.itrules.template.Template;

import java.util.ArrayList;
import java.util.List;

import static io.intino.itrules.template.condition.predicates.Predicates.*;
import static io.intino.itrules.template.outputs.Outputs.*;

public class ModelReaderTemplate extends Template {

	public List<Rule> ruleSet() {
		List<Rule> rules = new ArrayList<>();
		rules.add(rule().condition(allTypes("modelreader")).output(literal("package ")).output(placeholder("package")).output(literal(";\n\nimport ")).output(placeholder("package")).output(literal(".")).output(placeholder("outDsl", "firstUpperCase")).output(literal("Graph;\nimport io.intino.magritte.builder.StashBuilder;\n\nimport java.io.*;\nimport java.nio.file.*;\nimport java.util.*;\nimport java.util.stream.*;\nimport java.util.zip.*;\n\npublic class ModelReader {\n\tpublic static ")).output(placeholder("outDsl", "firstUpperCase")).output(literal("Graph loadFromZip(File ")).output(placeholder("outDsl")).output(literal("File) throws IOException {\n\t\tPath tempDir = Files.createTempDirectory(\"zip_extract_\");\n\t\ttry (ZipInputStream zis = new ZipInputStream(Files.newInputStream(")).output(placeholder("outDsl")).output(literal("File.toPath()))) {\n\t\t\tZipEntry entry;\n\t\t\twhile ((entry = zis.getNextEntry()) != null) {\n\t\t\t\tPath newPath = resolveZipEntry(tempDir, entry);\n\t\t\t\tif (entry.isDirectory()) Files.createDirectories(newPath);\n\t\t\t\telse {\n\t\t\t\t\tFiles.createDirectories(newPath.getParent());\n\t\t\t\t\ttry (OutputStream os = Files.newOutputStream(newPath)) {\n\t\t\t\t\t\tzis.transferTo(os);\n\t\t\t\t\t}\n\t\t\t\t}\n\t\t\t\tzis.closeEntry();\n\t\t\t}\n\t\t}\n\t\ttry (Stream<Path> files = Files.walk(tempDir)) {\n\t\t\treturn load(files\n\t\t\t\t\t\t.filter(Files::isRegularFile)\n\t\t\t\t\t\t.filter(path -> path.getFileName().toString().endsWith(\".tara\"))\n\t\t\t\t\t\t.filter(path -> !path.getFileName().toString().startsWith(\".\"))\n\t\t\t\t\t\t.map(Path::toFile)\n\t\t\t\t\t\t.toList());\n\t\t}\n\t}\n\n\tpublic static ")).output(placeholder("outDsl", "firstUpperCase")).output(literal("Graph load(File... ")).output(placeholder("outDsl")).output(literal("File) {\n\t\treturn load(List.of(")).output(placeholder("outDsl")).output(literal("File));\n\t}\n\n\tpublic static ")).output(placeholder("outDsl", "firstUpperCase")).output(literal("Graph load(List<File> ")).output(placeholder("outDsl")).output(literal("Files) {\n\t\treturn ")).output(placeholder("outDsl", "firstUpperCase")).output(literal("Graph.load(new StashBuilder(")).output(placeholder("outDsl")).output(literal("Files, \"")).output(placeholder("outDslCoors")).output(literal("\", \"accessor\", System.out).build());\n\t}\n\n\tprivate static Path resolveZipEntry(Path targetDir, ZipEntry zipEntry) throws IOException {\n\t\tPath resolvedPath = targetDir.resolve(zipEntry.getName()).normalize();\n\t\tif (!resolvedPath.startsWith(targetDir)) throw new IOException(\"Invalid Entry: \" + zipEntry.getName());\n\t\treturn resolvedPath;\n\t}\n}")));
		return rules;
	}

	public String render(Object object) {
		return new io.intino.itrules.Engine(this).render(object);
	}

	public String render(Object object, java.util.Map<String, io.intino.itrules.Formatter> formatters) {
		return new io.intino.itrules.Engine(this).addAll(formatters).render(object);
	}
}