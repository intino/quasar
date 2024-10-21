package io.intino.ime.box.scaffolds;

import io.intino.alexandria.logger.Logger;
import io.intino.ime.box.orchestator.QuassarParser;
import io.intino.ls.document.DocumentManager;

import java.net.URI;
import java.net.URISyntaxException;

import static io.intino.ime.box.orchestator.BuilderOrchestator.QUASSAR_FILE;

public class PycharmScaffold implements Scaffold {

	private final DocumentManager manager;
	private final String path;
	private final QuassarParser quassar;
	private String projectIml = """
			<?xml version="1.0" encoding="UTF-8"?>
			<module type="JAVA_MODULE" version="4">
			  <component name="NewModuleRootManager" inherit-compiler-output="true">
			    <exclude-output />
			    <content url="file:/$PROJECT_DIR$">
			      <sourceFolder url="file:/$PROJECT_DIR$src" isTestSource="false" />
			    </content>
			    <orderEntry type="inheritedJdk" />
			    <orderEntry type="sourceFolder" forTests="false" />
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

	private String mainCode = """
			package $package_name$;
			
			public class Main {
			    public static void main(String[] args) {
			        System.out.println("Hello, World!");
			    }
			}
			""";

	public PycharmScaffold(DocumentManager manager, String path){
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
			manager.upsertDocument(path("src/" + qn().toLowerCase().replace(".", "/") + "/Main.java"), process(mainCode));
		} catch (URISyntaxException e) {
			Logger.error(e);
		}

	}

	private String process(String projectIml) {
		return projectIml
				.replace("$project_name$", quassar.projectName())
				.replace("$java_version$", "21")
				.replace("$package_name$", qn())
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

	private String qn() {
		return "io.intino.quassar." + quassar.projectName();
	}

	private URI path(String subPath) throws URISyntaxException {
		return new URI(path + subPath);
	}

	@Override
	public String srcPath() {
		return "src";
	}

	@Override
	public String outPath() {
		return "out";
	}

	@Override
	public String genPath() {
		return "gen";
	}

	@Override
	public String resPath() {
		return "res";
	}
}
