package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.ModelCommands;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.model.GavCoordinates;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import io.quassar.editor.model.Model;

import java.util.List;
import java.util.Set;

public class LanguageTemplate extends AbstractLanguageTemplate<EditorBox> {
	private Language language;
	private LanguageRelease release;
	private LanguageTab tab;

	public LanguageTemplate(EditorBox box) {
		super(box);
	}

	public void open(String language, String tab) {
		this.language = box().languageManager().get(language);
		this.release = null;
		this.tab = tab != null ? LanguageTab.from(tab) : LanguageTab.About;
		refresh();
	}

	public void openTab(String tab) {
		this.tab = tab != null ? LanguageTab.from(tab) : LanguageTab.About;
		refreshHeader();
		refreshViews(false);
	}

	public void openHelp(String language, String version) {
		this.language = box().languageManager().get(language);
		this.release = this.language.release(version);
		refresh();
	}

	@Override
	public void init() {
		super.init();
		helpBlock.onShow(e -> refreshHelpBlock());
		mainBlock.onInit(e -> initMainBlock());
		mainBlock.onShow(e -> refreshMainBlock());
	}

	@Override
	public void refresh() {
		super.refresh();
		notFoundBlock.visible(!PermissionsHelper.hasPermissions(language, session(), box()));
		refreshHeader();
		refreshBlocks();
	}

	private void refreshBlocks() {
		if (!PermissionsHelper.hasPermissions(language, session(), box())) {
			mainBlock.hide();
			helpBlock.hide();
			return;
		}
		if (release != null) {
			mainBlock.hide();
			helpBlock.show();
		}
		else {
			helpBlock.hide();
			mainBlock.show();
		}
	}

	private void refreshHelpBlock() {
		helpTitle.value(translate(LanguageHelper.title(LanguageTab.Help)).formatted(LanguageHelper.title(GavCoordinates.fromString(language, release)), release.version()));
		helpLogo.value(LanguageHelper.logo(language, box()));
		String content = box().languageManager().loadHelp(language, release);
		helpStamp.content("<div class='help'>" + content + "</div>");
		helpStamp.refresh();
	}

	private void initMainBlock() {
		languageExplorer.onExpand(e -> expandHome());
		languageExplorer.onCollapse(e -> collapseHome());
		mainBlock.mainContentBlock.homeBlock.onShow(e -> refreshViews(true));
		mainBlock.mainContentBlock.modelsBlock.onShow(e -> refreshModels());
		mainBlock.mainContentBlock.modelsBlock.modelsStamp.onCreateModel(e -> createModel());
	}

	private void expandHome() {
		mainBlock.mainContentBlock.refreshLayout(55, 45);
	}

	private void collapseHome() {
		mainBlock.mainContentBlock.refreshLayout(95, 4);
	}

	private void refreshMainBlock() {
		refreshViews(true);
		refreshModels();
	}

	private void refreshHeader() {
		headerStamp.visible(release == null && PermissionsHelper.hasPermissions(language, session(), box()));
		if (!headerStamp.isVisible()) return;
		headerStamp.language(language);
		headerStamp.tab(tab);
		headerStamp.refresh();
	}

	private void refreshViews(boolean invalidate) {
		languageExplorer.invalidateCache(invalidate);
		languageExplorer.language(language);
		languageExplorer.release(release != null ? release.version() : language.lastRelease().version());
		languageExplorer.tab(tab);
		languageExplorer.refresh();
	}

	private void refreshModels() {
		mainBlock.mainContentBlock.modelsBlock.modelsStamp.language(language);
		mainBlock.mainContentBlock.modelsBlock.modelsStamp.bindTo(modelsDialog);
		mainBlock.mainContentBlock.modelsBlock.modelsStamp.refresh();
	}

	private Model createModel() {
		LanguageRelease release = language.lastRelease();
		String name = ModelHelper.proposeName();
		return box().commands(ModelCommands.class).create(name, name, "", GavCoordinates.fromString(language, release), DisplayHelper.user(session()), username());
	}

}