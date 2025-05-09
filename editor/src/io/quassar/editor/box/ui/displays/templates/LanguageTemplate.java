package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.ModelCommands;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.ui.types.LanguageView;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.box.util.SessionHelper;
import io.quassar.editor.model.GavCoordinates;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import io.quassar.editor.model.Model;

import java.util.List;

import static io.quassar.editor.box.util.DisplayHelper.valueOrDefault;

public class LanguageTemplate extends AbstractLanguageTemplate<EditorBox> {
	private Language language;
	private LanguageRelease release;
	private LanguageTab tab;
	private LanguageView view;

	public LanguageTemplate(EditorBox box) {
		super(box);
	}

	public void open(String language, String tab, String view) {
		this.language = box().languageManager().get(language);
		this.release = null;
		this.tab = tab != null ? LanguageTab.from(tab) : SessionHelper.languageTab(session());
		this.view = view != null ? LanguageView.from(view) : SessionHelper.languageView(session());
		refresh();
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
		notFoundBlock.visible(language == null);
		refreshHeader();
		refreshBlocks();
	}

	private void refreshBlocks() {
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
		helpTitle.value(translate(title(LanguageView.Help)).formatted(LanguageHelper.title(GavCoordinates.fromString(language, release))));
		helpLogo.value(LanguageHelper.logo(language, box()));
		String content = box().languageManager().loadHelp(language, release);
		helpStamp.content(content);
		helpStamp.refresh();
	}

	private void initMainBlock() {
		mainBlock.mainContentBlock.homeBlock.onShow(e -> refreshHome());
		mainBlock.mainContentBlock.modelsBlock.onShow(e -> refreshModels());
	}

	private void refreshMainBlock() {
		mainBlock.mainContentBlock.modelsBlock.modelsStamp.onCreateModel(tab != LanguageTab.Examples ? e -> createModel() : null);
		refreshHome();
		refreshModels();
	}

	private void refreshHeader() {
		headerStamp.visible(release == null);
		if (!headerStamp.isVisible()) return;
		headerStamp.language(language);
		headerStamp.tab(tab);
		headerStamp.view(view);
		headerStamp.refresh();
	}

	private void refreshHome() {
		title.value(translate(title()).formatted(language.key().toLowerCase()));
		logo.value(LanguageHelper.logo(language, box()));
		refreshHelpVersions();
		refreshAbout();
	}

	private String title() {
		return title(view);
	}

	private String title(LanguageView view) {
		if (view == LanguageView.Help) return "%s help";
		return "about %s";
	}

	private void refreshHelpVersions() {
		mainBlock.mainContentBlock.homeBlock.viewsBlock.versionsBlock.visible(view == LanguageView.Help);
		if (!mainBlock.mainContentBlock.homeBlock.viewsBlock.versionsBlock.isVisible()) return;
		List<LanguageRelease> releases = language.releases();
		helps.clear();
		releases.forEach(r -> fill(r, helps.add()));
	}

	private void refreshAbout() {
		mainBlock.mainContentBlock.homeBlock.viewsBlock.aboutBlock.visible(view == null || view == LanguageView.About);
		if (!mainBlock.mainContentBlock.homeBlock.viewsBlock.aboutBlock.isVisible()) return;
		aboutTitle.value(language.title());
		aboutDescription.value(valueOrDefault(language.description()));
		aboutCitation.value(valueOrDefault(language.citation()));
		aboutLicense.value(valueOrDefault(language.license()));
	}

	private void fill(LanguageRelease release, LanguageReleaseHelp display) {
		display.language(language);
		display.release(release);
		display.refresh();
	}

	private void refreshModels() {
		mainBlock.mainContentBlock.modelsBlock.modelsStamp.language(language);
		mainBlock.mainContentBlock.modelsBlock.modelsStamp.tab(tab);
		mainBlock.mainContentBlock.modelsBlock.modelsStamp.refresh();
	}

	private Model createModel() {
		LanguageRelease release = language.lastRelease();
		String name = ModelHelper.proposeName();
		return box().commands(ModelCommands.class).create(name, name, "", GavCoordinates.fromString(language, release), DisplayHelper.user(session()), username());
	}

}