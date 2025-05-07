package io.quassar.editor.box.ui.datasources;

import io.intino.alexandria.ui.services.push.UISession;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.model.Language;

import java.util.List;

public class LanguagesWithReleaseDatasource extends LanguagesDatasource {

	public LanguagesWithReleaseDatasource(EditorBox box, UISession session) {
		super(box, session);
	}

	@Override
	protected List<Language> load() {
		List<Language> result = super.load();
		return result.stream().filter(r -> !r.releases().isEmpty()).toList();
	}
}
