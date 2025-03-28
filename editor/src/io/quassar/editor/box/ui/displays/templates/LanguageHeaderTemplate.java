package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.util.PathHelper;
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
		publicModelsText.visible(tab == null || tab == LanguageTab.PublicModels);
		publicModelsLink.visible(tab == LanguageTab.OwnerModels);
		if (publicModelsLink.isVisible()) publicModelsLink.address(a -> PathHelper.languagePath(a, language, LanguageTab.PublicModels));
		myModelsText.visible(user() != null && tab == LanguageTab.OwnerModels);
		myModelsLink.visible(user() != null && (tab == null || tab == LanguageTab.PublicModels));
		if (myModelsLink.isVisible()) myModelsLink.address(a -> PathHelper.languagePath(a, language, LanguageTab.OwnerModels));
	}

}