package io.intino.ime.box.ui.datasources;

import io.intino.alexandria.ui.services.push.UISession;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.models.ModelManager;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;

import java.util.List;

public class LanguageModelsDatasource extends ModelsDatasource {
	private final Language language;

	public LanguageModelsDatasource(ImeBox box, UISession session, Language language) {
		super(box, session, false);
		this.language = language;
	}

	@Override
	protected List<Model> load() {
		ModelManager manager = box.modelManager();
		return manager.allModels().stream().filter(m -> ModelHelper.canOpenModel(m, username()) && language.name().equals(Language.nameOf(m.modelingLanguage()))).toList();
	}
}
