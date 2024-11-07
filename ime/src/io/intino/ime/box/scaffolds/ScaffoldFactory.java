package io.intino.ime.box.scaffolds;

import io.intino.ls.document.DocumentManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScaffoldFactory {


	private final Map<Language, List<Scaffold>> map = new HashMap<>() {{
		put(Language.Java, List.of(Scaffold.Intellij, Scaffold.MavenIntellij));
		put(Language.Python, List.of(Scaffold.Pycharm));
	}};

	;

	public static io.intino.ime.box.scaffolds.Scaffold scaffoldOf(Scaffold scaffold, DocumentManager manager, String path) {
		return scaffold == Scaffold.Intellij ? new IntellijScaffold(manager, path) :
				scaffold == Scaffold.MavenIntellij ? new MavenIntellijScaffold(manager, path) :
						new PycharmScaffold(manager, path);
	}

	public List<Language> languages() {
		return List.of(Language.values());
	}

	public List<Scaffold> scaffoldsOf(Language language) {
		return map.getOrDefault(language, Collections.emptyList());
	}

	public enum Language {Java, Python;}

	public enum Scaffold {Intellij, MavenIntellij, Pycharm}

}
