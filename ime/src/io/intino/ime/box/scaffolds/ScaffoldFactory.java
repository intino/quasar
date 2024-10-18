package io.intino.ime.box.scaffolds;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScaffoldFactory {

	public enum Language{Java, Python};
	public enum Scaffold{Intellij, Pycharm};

	private final Map<Language, List<Scaffold>> map = new HashMap<>(){{
		put(Language.Java, List.of(Scaffold.Intellij));
		put(Language.Python, List.of(Scaffold.Pycharm));
	}};

	public List<Language> languages(){
		return List.of(Language.values());
	}

	public List<Scaffold> scaffoldsOf(Language language) {
		return map.get(language);
	}

}
