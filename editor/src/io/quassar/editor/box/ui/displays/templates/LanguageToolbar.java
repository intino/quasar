package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import io.quassar.editor.model.LogoSize;

public class LanguageToolbar extends AbstractLanguageToolbar<EditorBox> {
	private Language language;
	private LanguageTab tab;

	public LanguageToolbar(EditorBox box) {
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
		refreshAbout();
		refreshExamples();
		refreshHelp();
		refreshLogo();
	}

	private void refreshAbout() {
		aboutText.visible(tab == LanguageTab.About);
		aboutLink.visible(tab != LanguageTab.About);
		if (aboutText.isVisible()) aboutText.value(translate("about"));
		if (aboutLink.isVisible()) {
			aboutLink.address(a -> PathHelper.languagePath(a, language, LanguageTab.About));
			aboutLink.title(translate("about"));
		}
	}

	private void refreshHelp() {
		helpText.visible(tab == LanguageTab.Help);
		helpLink.visible(tab != LanguageTab.Help);
		if (helpLink.isVisible()) helpLink.address(a -> PathHelper.languagePath(a, language, LanguageTab.Help));
	}

	private void refreshExamples() {
		boolean hasExamples = LanguageHelper.hasExamples(language);
		examplesText.visible(hasExamples && tab == LanguageTab.Examples);
		examplesLink.visible(hasExamples && tab != LanguageTab.Examples);
		if (examplesLink.isVisible()) examplesLink.address(a -> PathHelper.languagePath(a, language, LanguageTab.Examples));
	}

	private void refreshLogo() {
		languageLogo.value(LanguageHelper.logo(language, LogoSize.S50, box()));
	}

}