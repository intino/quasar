package io.intino.ime.box.scaffolds;

import io.intino.alexandria.logger.Logger;
import io.intino.ime.box.orchestator.QuassarParser;
import io.intino.ls.document.DocumentManager;

import java.net.URI;
import java.net.URISyntaxException;

import static io.intino.ime.box.orchestator.BuilderOrchestrator.QUASSAR_FILE;

public class MavenIntellijScaffold implements Scaffold {

	private final DocumentManager manager;
	private final String path;
	private final QuassarParser quassar;
	private String projectIml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <module type="JAVA_MODULE" version="4">
              <component name="NewModuleRootManager" inherit-compiler-output="true">
                <exclude-output />
                <content url="file:/$PROJECT_DIR$">
                  <sourceFolder url="file:/$PROJECT_DIR$src/main/java" isTestSource="false" />
                  <sourceFolder url="file:/$PROJECT_DIR$src/test/java" isTestSource="true" />
                </content>
                <orderEntry type="inheritedJdk" />
                <orderEntry type="sourceFolder" forTests="false" />
                <orderEntry type="library" name="Maven: org.apache.maven:maven-core:3.8.1" level="project" />
              </component>
            </module>
            """;

	private String miscFile = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project version="4">
              <component name="ProjectRootManager" version="2" languageLevel="JDK_1_8" default="false" project-jdk-name="$java_version$" project-jdk-type="JavaSDK" />
            </project>
            """;

	private String modulesFile = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project version="4">
              <component name="ProjectModuleManager">
                <modules>
                  <module fileurl="file:/$PROJECT_DIR$/$project_name$.iml" filepath="$PROJECT_DIR$/$project_name$.iml" />
                </modules>
              </component>
            </project>
            """;

	private String pomFile = """
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                <modelVersion>4.0.0</modelVersion>
                <groupId>$group_id$</groupId>
                <artifactId>$artifact_id$</artifactId>
                <version>1.0-SNAPSHOT</version>
                <properties>
                    <maven.compiler.source>1.8</maven.compiler.source>
                    <maven.compiler.target>1.8</maven.compiler.target>
                </properties>
                <dependencies>
                    <dependency>
                        <groupId>junit</groupId>
                        <artifactId>junit</artifactId>
                        <version>4.12</version>
                        <scope>test</scope>
                    </dependency>
                </dependencies>
            </project>
            """;

	private String mainCode = """
            package $package_name$;
            
            public class Main {
                public static void main(String[] args) {
                    System.out.println("Hello, World!");
                }
            }
            """;

	public MavenIntellijScaffold(DocumentManager manager, String path){
		this.manager = manager;
		this.path = path.endsWith("/") ? path : path + "/";
		this.quassar = new QuassarParser(quassarContent());
	}

	private String quassarContent() {
		try {
			return new String(manager.getDocumentText(new URI(QUASSAR_FILE)).readAllBytes());
		} catch (Exception e) {
			Logger.error(e);
			return "";
		}
	}

	@Override
	public void build(){
		try {
			manager.upsertDocument(path(quassar.projectName() + ".iml"), process(projectIml));
			manager.upsertDocument(path(".idea/misc.xml"), process(miscFile));
			manager.upsertDocument(path(".idea/modules.xml"), process(modulesFile));
			manager.upsertDocument(path("pom.xml"), process(pomFile));
			manager.upsertDocument(path("src/main/java/" + quassar.codePackage().toLowerCase().replace(".", "/") + "/Main.java"), process(mainCode));
		} catch (URISyntaxException e) {
			Logger.error(e);
		}
	}

	@Override
	public String srcPath() {
		return "src/main/java";
	}

	@Override
	public String outPath() {
		return "target";
	}

	@Override
	public String genPath() {
		return "target/generated-sources";
	}

	@Override
	public String resPath() {
		return "src/main/resources";
	}

	private String process(String template) {
		return template
				.replace("$project_name$", quassar.projectName())
				.replace("$java_version$", "21")
				.replace("$package_name$", quassar.codePackage())
				.replace("$group_id$", "io.intino.quassar")
				.replace("$artifact_id$", quassar.projectName())
				.replace("$PROJECT_DIR$", projectDir());
	}

	private String projectDir() {
		try {
			return new URI(manager.root().getPath() + path).getPath();
		} catch (URISyntaxException e) {
			Logger.error(e);
			return null;
		}
	}

	private URI path(String subPath) throws URISyntaxException {
		return new URI(path + subPath);
	}
}
