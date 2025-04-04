package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.displays.UserMessage;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.LanguageCommands;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.box.util.SessionHelper;
import io.quassar.editor.model.Language;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class LanguageTemplate extends AbstractLanguageTemplate<EditorBox> {
	private Language language;
	private LanguageTab tab;

	public LanguageTemplate(EditorBox box) {
		super(box);
	}

	public void open(String language, String tab) {
		this.language = box().languageManager().get(language);
		this.tab = tab != null ? LanguageTab.from(tab) : SessionHelper.languageTab(session());
		refresh();
	}

	@Override
	public void init() {
		super.init();
		homeBlock.onShow(e -> refreshHome());
		modelsBlock.onShow(e -> refreshModels());
		headerStamp.onSaveSettings(e -> refresh());
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
		headerStamp.refresh();
	}

	private void refreshContent() {
		refreshHome();
		refreshModels();
	}

	private void refreshHome() {
		title.value(language.name());
		logo.value(LanguageHelper.logo(language, box()));
		refreshHomeReadme();
	}

	private void refreshHomeReadme() {
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
		modelsBlock.modelsStamp.tab(tab);
		modelsBlock.modelsStamp.refresh();
	}

}