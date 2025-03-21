package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.components.BlockConditional;
import io.quassar.editor.box.EditorBox;

import java.util.Arrays;

public class HomeTemplate extends AbstractHomeTemplate<EditorBox> {
	private Page current;

	public HomeTemplate(EditorBox box) {
		super(box);
	}

	public enum Page {
		Languages, Language, Model;

		public static Page from(String key) {
			return Arrays.stream(values()).filter(v -> v.name().equalsIgnoreCase(key)).findFirst().orElse(null);
		}
	}

	public void openHome() {
		openLanguages(null, null);
	}

	public void openLanguages(String tab, String view) {
		openPage(Page.Languages);
		if (languagesPage.languagesStamp != null) languagesPage.languagesStamp.open(tab, view);
	}

	public void openLanguage(String language, String tab, String view) {
		openPage(Page.Language);
		if (languagePage.languageStamp != null) languagePage.languageStamp.open(language, tab, view);
	}

	public void openModel(String language, String model, String version, String file) {
		openPage(Page.Model);
		if (modelPage.modelStamp != null) modelPage.modelStamp.open(language, model, version, file);
	}

	private boolean openPage(Page page) {
		if (current == page) return true;
		header.refresh();
		loading.visible(false);
		hidePages();
		blockOf(page).show();
		current = page;
		return true;
	}

	private void hidePages() {
		if (languagesPage.isVisible()) languagesPage.hide();
		if (languagePage.isVisible()) languagePage.hide();
		if (modelPage.isVisible()) modelPage.hide();
	}

	private BlockConditional<?, ?> blockOf(Page page) {
		if (page == Page.Languages) return languagesPage;
		if (page == Page.Language) return languagePage;
		if (page == Page.Model) return modelPage;
		return null;
	}

}