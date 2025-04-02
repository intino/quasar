package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.components.BlockConditional;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.ui.types.ModelView;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.util.Arrays;

public class HomeTemplate extends AbstractHomeTemplate<EditorBox> {
	private Page current;
	private Language language;
	private Model model;

	public HomeTemplate(EditorBox box) {
		super(box);
	}

	public enum Page {
		About, Languages, Language, Model;

		public static Page from(String key) {
			return Arrays.stream(values()).filter(v -> v.name().equalsIgnoreCase(key)).findFirst().orElse(null);
		}
	}

	public void openHome() {
		openLanguages(null);
	}

	public void openAbout() {
		set(null, null);
		openPage(Page.About);
		if (aboutPage.aboutStamp != null) aboutPage.aboutStamp.open();
	}

	public void openLanguages(String tab) {
		set(null, null);
		openPage(Page.Languages);
		if (languagesPage.languagesStamp != null) languagesPage.languagesStamp.open(tab);
	}

	public void openLanguage(String language, String tab) {
		set(language, null);
		openPage(Page.Language);
		if (languagePage.languageStamp != null) languagePage.languageStamp.open(language, tab);
	}

	public void openModel(String language, String model, String release, String view, String file, String position) {
		set(language, model);
		openPage(Page.Model);
		if (modelPage.modelStamp != null) modelPage.modelStamp.open(language, model, release, view, file, position);
	}

	private boolean openPage(Page page) {
		refreshHeader();
		if (current == page) return true;
		loading.visible(false);
		hidePages();
		blockOf(page).show();
		current = page;
		return true;
	}

	private void refreshHeader() {
		header.language(language);
		header.model(model);
		header.refresh();
	}

	private void hidePages() {
		if (aboutPage.isVisible()) aboutPage.hide();
		if (languagesPage.isVisible()) languagesPage.hide();
		if (languagePage.isVisible()) languagePage.hide();
		if (modelPage.isVisible()) modelPage.hide();
	}

	private BlockConditional<?, ?> blockOf(Page page) {
		if (page == Page.About) return aboutPage;
		if (page == Page.Languages) return languagesPage;
		if (page == Page.Language) return languagePage;
		if (page == Page.Model) return modelPage;
		return null;
	}

	private void set(String language, String model) {
		this.language = language != null ? box().languageManager().get(language) : null;
		this.model = model != null ? box().modelManager().get(this.language, model) : null;
	}

}