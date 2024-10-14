package io.intino.ime.box.scaffolds;

public class IntellijScaffold {

	private String projectIml = """
			<?xml version="1.0" encoding="UTF-8"?>
			<module type="JAVA_MODULE" version="4">
			  <component name="NewModuleRootManager" inherit-compiler-output="true">
			    <exclude-output />
			    <content url="file://$PROJECT_DIR$">
			      <sourceFolder url="file://$PROJECT_DIR$/src" isTestSource="false" />
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
			      <module fileurl="file://$PROJECT_DIR$/.idea/$project_name$.iml" filepath="$PROJECT_DIR$/.idea/$project_name$.iml" />
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
}
