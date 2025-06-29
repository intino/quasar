package io.quassar.builder.modelparser;

import io.intino.itrules.template.Rule;
import io.intino.itrules.template.Template;

import java.util.ArrayList;
import java.util.List;

import static io.intino.itrules.template.condition.predicates.Predicates.*;
import static io.intino.itrules.template.outputs.Outputs.*;

public class POMTemplate extends Template {

	public List<Rule> ruleSet() {
		List<Rule> rules = new ArrayList<>();
		rules.add(rule().condition(allTypes("pom")).output(literal("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<project xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\" xmlns=\"http://maven.apache.org/POM/4.0.0\"\n    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n\t<modelVersion>4.0.0</modelVersion>\n\t<groupId>")).output(placeholder("groupId", "lowercase")).output(literal(".")).output(placeholder("name", "lowercase")).output(literal("</groupId>\n\t<artifactId>parser-java</artifactId>\n\t<version>")).output(placeholder("version")).output(literal("</version>\n\t<properties>\n\t\t<maven.compiler.source>21</maven.compiler.source>\n\t\t<maven.compiler.target>21</maven.compiler.target>\n\t</properties>\n\t<repositories>\n\t\t<repository>\n\t\t\t<id>intino-maven</id>\n\t\t\t<name>intino-maven-releases</name>\n\t\t\t<url>https://artifactory.intino.io/artifactory/releases</url>\n\t\t</repository>\n\t\t<repository>\n\t\t\t <id>Quassar</id>\n\t\t\t <url>https://quassar.io/artifacts/releases</url>\n        </repository>\n\t</repositories>\n\n\t<build>\n\t\t<outputDirectory>out/production/")).output(placeholder("name")).output(literal("-model-parser-java</outputDirectory>\n\t\t<directory>out/build/")).output(placeholder("name")).output(literal("-model-parser-java</directory>\n\t\t<plugins>\n\t\t\t<plugin>\n\t\t\t\t<groupId>org.codehaus.mojo</groupId>\n\t\t\t\t<artifactId>build-helper-maven-plugin</artifactId>\n\t\t\t\t<version>3.4.0</version>\n\t\t\t\t<executions>\n\t\t\t\t\t<execution>\n\t\t\t\t\t\t<id>add-source</id>\n\t\t\t\t\t\t<phase>generate-sources</phase>\n\t\t\t\t\t\t<goals>\n\t\t\t\t\t\t\t<goal>add-source</goal>\n\t\t\t\t\t\t</goals>\n\t\t\t\t\t\t<configuration>\n\t\t\t\t\t\t\t<sources>\n\t\t\t\t\t\t\t\t<source>gen</source>\n\t\t\t\t\t\t\t\t<source>src</source>\n\t\t\t\t\t\t\t</sources>\n\t\t\t\t\t\t</configuration>\n\t\t\t\t\t</execution>\n\t\t\t\t</executions>\n\t\t\t</plugin>\n\t\t\t<plugin>\n\t\t\t\t<groupId>org.apache.maven.plugins</groupId>\n\t\t\t\t<artifactId>maven-source-plugin</artifactId>\n\t\t\t\t<version>3.3.1</version>\n\t\t\t\t<executions>\n\t\t\t\t\t<execution>\n\t\t\t\t\t\t<id>attach-sources</id>\n\t\t\t\t\t\t<goals>\n\t\t\t\t\t\t\t<goal>jar-no-fork</goal>\n\t\t\t\t\t\t</goals>\n\t\t\t\t\t</execution>\n\t\t\t\t</executions>\n\t\t\t</plugin>\n\t\t</plugins>\n\t</build>\n\n\t<dependencies>\n\t\t<dependency>\n\t\t\t<groupId>")).output(placeholder("groupId", "lowercase")).output(literal("</groupId>\n\t\t\t<artifactId>")).output(placeholder("name", "lowercase")).output(literal("</artifactId>\n\t\t\t<version>")).output(placeholder("version")).output(literal("</version>\n\t\t\t<scope>compile</scope>\n\t\t</dependency>\n\t\t<dependency>\n\t\t\t<groupId>io.intino.magritte</groupId>\n\t\t\t<artifactId>builder</artifactId>\n\t\t\t<version>8.0.0</version>\n\t\t\t<scope>compile</scope>\n\t\t</dependency>\n\t\t<dependency>\n\t\t\t<groupId>io.intino.magritte</groupId>\n\t\t\t<artifactId>framework</artifactId>\n\t\t\t<version>5.2.1</version>\n\t\t\t<scope>compile</scope>\n\t\t</dependency>\n\t</dependencies>\n</project>")));
		return rules;
	}

	public String render(Object object) {
		return new io.intino.itrules.Engine(this).render(object);
	}

	public String render(Object object, java.util.Map<String, io.intino.itrules.Formatter> formatters) {
		return new io.intino.itrules.Engine(this).addAll(formatters).render(object);
	}
}