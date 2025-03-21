package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.ui.types.LanguageView;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.model.Language;

public class LanguageHeaderTemplate extends AbstractLanguageHeaderTemplate<EditorBox> {
	private Language language;
	private LanguageTab tab;
	private LanguageView view;

	public LanguageHeaderTemplate(EditorBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void tab(LanguageTab tab) {
		this.tab = tab;
	}

	public void view(LanguageView view) {
		this.view = view;
	}

	@Override
	public void refresh() {
		super.refresh();
		if (language == null) return;
		modelsToolbar.visible(tab == LanguageTab.Models);
		title.value(language.name());
		logo.value(box().archetype().languages().logo(language.name()));
		publicModels.readonly(view == null || view == LanguageView.PublicModels);
		publicModels.address(a -> PathHelper.languagePath(a, language, LanguageTab.Models, LanguageView.PublicModels));
		myModels.readonly(view == LanguageView.OwnerModels);
		myModels.visible(user() != null);
		myModels.address(a -> PathHelper.languagePath(a, language, LanguageTab.Models, LanguageView.OwnerModels));
		homeOperation.readonly(tab == null || tab == LanguageTab.Home);
		homeOperation.address(a -> PathHelper.languagePath(a, language, LanguageTab.Home));
		modelsOperation.readonly(tab == LanguageTab.Models);
		modelsOperation.address(a -> PathHelper.languagePath(a, language, LanguageTab.Models));
	}

}