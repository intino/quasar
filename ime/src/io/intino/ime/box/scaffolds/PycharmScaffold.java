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
	private final String projectFile = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project version="4">
              <component name="ProjectRootManager" version="2" languageLevel="Python 3.8" default="false" project-jdk-name="$python_version$" project-jdk-type="Python SDK" />
            </project>
            """;

	private final String modulesFile = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project version="4">
              <component name="ProjectModuleManager">
                <modules>
                  <module fileurl="file:/$PROJECT_DIR$/$project_name$.iml" filepath="$PROJECT_DIR$/$project_name$.iml" />
                </modules>
              </component>
            </project>
            """;

	private final String mainCode = """
            def main():
                print("Hello, World!")
           
            if __name__ == "__main__":
                main()
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
			manager.upsertDocument(path(quassar.projectName() + ".iml"), process(projectFile));
			manager.upsertDocument(path(".idea/modules.xml"), process(modulesFile));
			manager.upsertDocument(path("src/" + quassar.codePackage().toLowerCase().replace(".", "/") + "/main.py"), process(mainCode));
		} catch (URISyntaxException e) {
			Logger.error(e);
		}
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

	private String process(String projectFile) {
		return projectFile
				.replace("$project_name$", quassar.projectName())
				.replace("$python_version$", "3.8")
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
