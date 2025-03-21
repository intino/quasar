package io.quassar.editor.model;

import java.util.List;

import static java.util.Collections.emptyList;

public interface Browser {
	Model model(String language, String id);
	Language language(String name);

	interface Language {
		String name();
		Language parent();
		Model metamodel();
		List<Model> children();
	}
	
	interface Model {
		String id();
		default Model parent() {
			return modelingLanguage().metamodel();
		}
		
		Language modelingLanguage();
		Language releasedLanguage();
		
		default List<Model> children() {
			return releasedLanguage() != null ? releasedLanguage().children() : emptyList();
		}
	}
	
}