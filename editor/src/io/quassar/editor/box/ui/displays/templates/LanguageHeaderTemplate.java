package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.model.Language;

public class LanguageHeaderTemplate extends AbstractLanguageHeaderTemplate<EditorBox> {
	private Language language;
	private LanguageTab tab;

	public LanguageHeaderTemplate(EditorBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void tab(LanguageTab tab) {
		this.tab = tab;
	}

	@Override
	public void refresh() {
		super.refresh();
		if (language == null) return;
		title.visible(user() != null);
		if (title.isVisible()) title.value(translate("%s models").formatted(LanguageHelper.label(language, this::translate)));
		notLoggedBlock.visible(user() == null);
		toolbar.language(language);
		toolbar.tab(tab);
		toolbar.refresh();
	}

}