package io.intino.ime.box.ui.datasources;

import io.intino.alexandria.ui.services.push.UISession;
import io.intino.ime.box.ImeBox;
import io.intino.ime.model.Language;

import java.util.List;

public class MemoryLanguagesDatasource extends LanguagesDatasource {
	private final List<Language> languages;

	public MemoryLanguagesDatasource(ImeBox box, UISession session, List<Language> languages) {
		super(box, session);
		this.languages = languages;
	}

	@Override
	protected List<Language> load() {
		return languages;
	}
}
