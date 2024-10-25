package modelaccessor;

import io.intino.itrules.template.Rule;
import io.intino.itrules.template.Template;

import java.util.ArrayList;
import java.util.List;

import static io.intino.itrules.template.condition.predicates.Predicates.*;
import static io.intino.itrules.template.outputs.Outputs.*;

public class ModelAccessorTemplate extends Template {

	public List<Rule> ruleSet() {
		List<Rule> rules = new ArrayList<>();
		rules.add(rule().condition(allTypes("modelaccessor")).output(literal("package ")).output(placeholder("package")).output(literal(";\n\nimport io.flogo.model.")).output(placeholder("outDsl", "firstUpperCase")).output(literal("Graph;\nimport io.intino.magritte.builder.StashBuilder;\n\nimport java.io.File;\nimport java.util.List;\n\npublic class ModelAccessor {\n\tpublic static ")).output(placeholder("outDsl", "firstUpperCase")).output(literal("Graph load(File ")).output(placeholder("outDsl")).output(literal("File) {\n\t\treturn load(List.of(")).output(placeholder("outDsl")).output(literal("File));\n\t}\n\n\tpublic static ")).output(placeholder("outDsl", "firstUpperCase")).output(literal("Graph load(List<File> ")).output(placeholder("outDsl")).output(literal("Files) {\n\t\treturn ")).output(placeholder("outDsl", "firstUpperCase")).output(literal("Graph.load(new StashBuilder(")).output(placeholder("outDsl")).output(literal("Files, \"")).output(placeholder("outDsl")).output(literal("\", \"")).output(placeholder("version")).output(literal("\", \"accessor\", System.out).build());\n\t}\n}")));
		return rules;
	}

	public String render(Object object) {
		return new io.intino.itrules.Engine(this).render(object);
	}

	public String render(Object object, java.util.Map<String, io.intino.itrules.Formatter> formatters) {
		return new io.intino.itrules.Engine(this).addAll(formatters).render(object);
	}
}