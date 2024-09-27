package io.intino.ime.box;

import io.intino.ime.model.Browser;

import java.util.List;

public class ImeBrowser implements Browser {
	private final ImeBox box;

	public ImeBrowser(ImeBox box) {
		this.box = box;
	}

	@Override
	public Model model(String id) {
		return modelOf(box.modelManager().model(id));
	}

	@Override
	public Language language(String name) {
		return languageOf(box.languageManager().get(name));
	}

	private Model modelOf(io.intino.ime.model.Model model) {
		return new Model() {
			@Override
			public String id() {
				return model.id();
			}

			@Override
			public Language modelingLanguage() {
				return languageOf(model.modelingLanguage());
			}

			@Override
			public Language releasedLanguage() {
				return model.releasedLanguage() != null && !model.releasedLanguage().isEmpty() ? languageOf(model.releasedLanguage()) : null;
			}
		};
	}

	private Language languageOf(String language) {
		return languageOf(box.languageManager().get(language));
	}

	private Language languageOf(io.intino.ime.model.Language language) {
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
				return modelOf(box.modelManager().modelWith(language));
			}

			@Override
			public List<Model> children() {
				return box.modelManager().models(language).stream().map(m -> modelOf(m)).toList();
			}
		};
	}
}
