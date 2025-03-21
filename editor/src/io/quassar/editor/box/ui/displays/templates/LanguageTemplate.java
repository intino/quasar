package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.logger.Logger;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.ui.types.LanguageView;
import io.quassar.editor.box.util.SessionHelper;
import io.quassar.editor.model.Language;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class LanguageTemplate extends AbstractLanguageTemplate<EditorBox> {
	private Language language;
	private LanguageTab tab;
	private LanguageView view;

	public LanguageTemplate(EditorBox box) {
		super(box);
	}

	public void open(String language, String tab, String view) {
		this.language = box().languageManager().get(language);
		this.tab = tab != null ? LanguageTab.from(tab) : SessionHelper.languageTab(session());
		this.view = view != null ? LanguageView.from(view) : SessionHelper.languageView(session());
		refresh();
	}

	@Override
	public void init() {
		super.init();
		homeBlock.onShow(e -> refreshHome());
		modelsBlock.onShow(e -> refreshModels());
	}

	@Override
	public void refresh() {
		super.refresh();
		notFoundBlock.visible(language == null);
		refreshHeader();
		refreshContent();
	}

	private void refreshHeader() {
		headerStamp.language(language);
		headerStamp.tab(tab);
		headerStamp.view(view);
		headerStamp.refresh();
	}

	private void refreshContent() {
		contentBlock.visible(language != null);
		if (!contentBlock.isVisible()) return;
		homeBlock.visible(tab == null || tab == LanguageTab.Home);
		modelsBlock.visible(tab == LanguageTab.Models);
	}

	private void refreshHome() {
		try {
			File readme = box().archetype().languages().readme(language.name());
			if (!readme.exists()) return;
			homeStamp.content(Files.readString(readme.toPath()));
			homeStamp.refresh();
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private void refreshModels() {
		modelsBlock.modelsStamp.language(language);
		modelsBlock.modelsStamp.view(view);
		modelsBlock.modelsStamp.refresh();
	}

}