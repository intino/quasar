package io.quassar.editor.model;

import io.quassar.editor.box.EditorBox;

import java.util.List;

public class EditorBrowser implements Browser {
	private final EditorBox box;

	public EditorBrowser(EditorBox box) {
		this.box = box;
	}

	@Override
	public Model model(String language, String id) {
		return modelOf(box.modelManager().get(language, id));
	}

	@Override
	public Language language(String name) {
		return languageOf(box.languageManager().get(name));
	}

	private Model modelOf(io.quassar.editor.model.Model model) {
		return new Model() {
			@Override
			public String id() {
				return model.name();
			}

			@Override
			public Language modelingLanguage() {
				return languageOf(model.language());
			}

			@Override
			public Language releasedLanguage() {
				return languageOf(model.name());
			}
		};
	}

	private Language languageOf(String language) {
		if (language == null) return null;
		return languageOf(box.languageManager().get(language));
	}

	private Language languageOf(io.quassar.editor.model.Language language) {
		if (language == null) return null;
		return new Language() {
			@Override
			public String name() {
				return language.name();
			}

			@Override
			public Language parent() {
				return languageOf(language.parent());
			}

			@Override
			public Model metamodel() {
				Language parent = parent();
				if (parent == null) return null;
				return modelOf(box.modelManager().get(language.parent(), parent().name()));
			}

			@Override
			public List<Model> children() {
				return box.modelManager().models(language).stream().map(m -> modelOf(m)).toList();
			}
		};
	}
}
