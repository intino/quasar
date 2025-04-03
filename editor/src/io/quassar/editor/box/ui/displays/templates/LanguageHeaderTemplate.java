package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.model.Language;

import java.util.function.Consumer;

public class LanguageHeaderTemplate extends AbstractLanguageHeaderTemplate<EditorBox> {
	private Language language;
	private LanguageTab tab;
	private Consumer<Language> editReadmeListener;

	public LanguageHeaderTemplate(EditorBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void tab(LanguageTab tab) {
		this.tab = tab;
	}

	public void onEditReadme(Consumer<Language> listener) {
		this.editReadmeListener = listener;
	}

	@Override
	public void init() {
		super.init();
		editReadmeTrigger.onExecute(e -> editReadmeListener.accept(language));
	}

	@Override
	public void refresh() {
		super.refresh();
		if (language == null) return;
		publicModelsText.visible(tab == LanguageTab.PublicModels);
		publicModelsLink.visible(tab == LanguageTab.OwnerModels);
		if (publicModelsLink.isVisible()) publicModelsLink.address(a -> PathHelper.languagePath(a, language, LanguageTab.PublicModels));
		myModelsText.visible(user() != null && (tab == null || tab == LanguageTab.OwnerModels));
		myModelsLink.visible(user() != null && (tab == null || tab == LanguageTab.PublicModels));
		if (myModelsLink.isVisible()) myModelsLink.address(a -> PathHelper.languagePath(a, language, LanguageTab.OwnerModels));
		gotoModelTrigger.visible(PermissionsHelper.canOpenModel(language, session(), box()));
		if (gotoModelTrigger.isVisible()) gotoModelTrigger.address(path -> PathHelper.modelPath(LanguageHelper.model(language, box())));
		editReadmeTrigger.visible(PermissionsHelper.canEdit(language, session(), box()));
	}

}