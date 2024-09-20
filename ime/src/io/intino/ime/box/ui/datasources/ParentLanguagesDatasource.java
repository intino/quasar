package io.intino.ime.box.ui.datasources;

import io.intino.alexandria.ui.services.push.UISession;
import io.intino.ime.box.ImeBox;
import io.intino.ime.model.Language;

import java.util.List;

public class ParentLanguagesDatasource extends LanguagesDatasource {
	private final Language parent;

	public ParentLanguagesDatasource(ImeBox box, UISession session, Language parent) {
		super(box, session);
		this.parent = parent;
	}

	@Override
	protected List<Language> load() {
		return box.languageManager().publicLanguages(username()).stream().filter(l -> l.parent() != null && Language.nameOf(l.parent()).equals(parent.name())).toList();
	}
}
