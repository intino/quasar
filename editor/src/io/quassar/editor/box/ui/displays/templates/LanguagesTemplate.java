package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.displays.events.AddCollectionItemEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.ui.datasources.LanguagesDatasource;
import io.quassar.editor.box.ui.displays.items.LanguageMagazineItem;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.ui.types.LanguagesTab;
import io.quassar.editor.box.ui.types.LanguagesView;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.box.util.SessionHelper;
import io.quassar.editor.model.Language;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class LanguagesTemplate extends AbstractLanguagesTemplate<EditorBox> {
	private LanguagesTab tab;
	private LanguagesView view;

	public LanguagesTemplate(EditorBox box) {
		super(box);
	}

	public void open(String tab, String view) {
		this.tab = tab != null ? LanguagesTab.from(tab) : SessionHelper.languagesTab(session());
		this.view = view != null ? LanguagesView.from(view) : SessionHelper.languagesView(session());
		refresh();
	}

	public void filter(String condition) {
		languagesMagazine.filter(condition);
	}

	public void filter(String grouping, List<String> values) {
		languagesMagazine.filter(grouping, values);
	}

	@Override
	public void init() {
		super.init();
		homeBlock.onShow(e -> refreshHomeBlock());
		languagesBlock.onInit(e -> initLanguagesBlock());
		languagesBlock.onShow(e -> refreshLanguageBlock());
	}

	@Override
	public void refresh() {
		super.refresh();
		refreshHeader();
		refreshContent();
	}

	private void initLanguagesBlock() {
		languagesMagazine.onAddItem(this::refresh);
	}

	private void refreshHeader() {
		headerStamp.tab(tab);
		headerStamp.view(view);
		headerStamp.refresh();
	}

	private void refreshContent() {
		homeBlock.visible(tab == null || tab == LanguagesTab.Home);
		languagesBlock.visible(tab == LanguagesTab.Languages);
	}

	private void refreshHomeBlock() {
		try {
			File readme = box().archetype().readme();
			if (!readme.exists()) return;
			homeStamp.content(Files.readString(readme.toPath()));
			homeStamp.refresh();
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private void refreshLanguageBlock() {
		languagesMagazine.source(new LanguagesDatasource(box(), session(), view));
	}

	private void refresh(AddCollectionItemEvent event) {
		Language language = event.item();
		LanguageMagazineItem item = event.component();
		item.logo.value(LanguageHelper.logo(language, box()));
		item.languageTitleLink.title(language.name());
		item.languageTitleLink.address(path -> PathHelper.languagePath(path, language));
		item.description.value(language.description());
		item.languageLink.address(path -> PathHelper.languagePath(path, language));
		item.owner.value(language.owner());
		item.createDate.value(language.createDate());
		item.modelsCount.value(language.modelsCount());
		item.parent.value(!language.isFoundational() ? language.parent() : "-");
		item.viewModels.readonly(language.modelsCount() == 0);
		item.viewModels.address(path -> PathHelper.languagePath(path, language, LanguageTab.Models));
	}

}