package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.ui.types.LanguageView;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;

import java.util.function.Consumer;

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
	public void init() {
		super.init();
	}

	@Override
	public void refresh() {
		super.refresh();
		if (language == null) return;
		String owner = box().languageManager().owner(language);
		metamodelLink.visible(false);
		//metamodelLink.visible(owner != null && owner.equals(username()));
		if (metamodelLink.isVisible()) metamodelLink.address(path -> PathHelper.modelPath(box().modelManager().get(language.metamodel())));
		refreshModels();
		refreshExamples();
		refreshHelp();
		refreshAbout();
	}

	private void refreshModels() {
		modelsText.visible(user() != null && tab == LanguageTab.Models);
		modelsLink.visible(user() != null && tab == LanguageTab.Examples);
		if (modelsLink.isVisible()) modelsLink.address(a -> PathHelper.languagePath(a, language, LanguageTab.Models, view));
	}

	private void refreshExamples() {
		examplesText.visible(tab == null || tab == LanguageTab.Examples);
		examplesLink.visible(tab == null || tab == LanguageTab.Models);
		if (examplesLink.isVisible()) examplesLink.address(a -> PathHelper.languagePath(a, language, LanguageTab.Examples, view));
	}

	private void refreshHelp() {
		helpText.visible(view == LanguageView.Help);
		helpLink.visible(view == LanguageView.About);
		if (helpLink.isVisible()) helpLink.address(a -> PathHelper.languagePath(a, language, tab, LanguageView.Help));
	}

	private void refreshAbout() {
		aboutText.visible(view == LanguageView.About);
		aboutLink.visible(view == LanguageView.Help);
		if (aboutLink.isVisible()) aboutLink.address(a -> PathHelper.languagePath(a, language, tab, LanguageView.About));
	}

}