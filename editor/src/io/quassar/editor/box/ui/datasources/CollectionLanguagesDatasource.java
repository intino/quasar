package io.quassar.editor.box.ui.datasources;

import io.intino.alexandria.ui.services.push.UISession;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.model.Collection;
import io.quassar.editor.model.Language;

import java.util.List;

public class CollectionLanguagesDatasource extends LanguagesDatasource {
	private final Collection collection;

	public CollectionLanguagesDatasource(EditorBox box, UISession session, Collection collection) {
		super(box, session);
		this.collection = collection;
	}

	@Override
	protected List<Language> load() {
		return box.languageManager().languages(collection);
	}
}
