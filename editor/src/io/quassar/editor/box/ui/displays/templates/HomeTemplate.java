package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.displays.components.BlockConditional;
import io.intino.alexandria.ui.services.push.User;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.ModelCommands;
import io.quassar.editor.box.commands.UserCommands;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.model.GavCoordinates;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import io.quassar.editor.model.Model;

import java.util.Arrays;

public class HomeTemplate extends AbstractHomeTemplate<EditorBox> {
	private Page current;
	private Language language;
	private String release;
	private Model model;

	public HomeTemplate(EditorBox box) {
		super(box);
	}

	public enum Page {
		Landing, About, Language, Model, Forge;

		public static Page from(String key) {
			return Arrays.stream(values()).filter(v -> v.name().equalsIgnoreCase(key)).findFirst().orElse(null);
		}
	}

	@Override
	public void init() {
		super.init();
		registerUserIfNeeded();
	}

	public void createModel(String languageId) {
		Language language = box().languageManager().get(languageId);
		if (language == null) {
			Logger.warn("Trying to create model from not recognized language");
			notifier.redirect(PathHelper.languageUrl(languageId, session()));
			return;
		}
		if (!PermissionsHelper.canAddModel(language, session(), box())) {
			session().add("callback", session().browser().requestUrl());
			notifier.redirect(PathHelper.loginUrl(session()));
			return;
		}
		String name = ModelHelper.proposeName();
		if (language.lastRelease() == null) {
			Logger.warn("Trying to create model from language " + language.name() + " with no releases");
			return;
		}
		Model model = box().commands(ModelCommands.class).create(name, name, "", new GavCoordinates(language.group(), language.name(), language.lastRelease().version()), username(), username());
		notifier.redirect(PathHelper.modelUrl(model, session()));
	}

	public void openHome(String dialog) {
		openLanding(dialog);
	}

	public void openLanding(String dialog) {
		set((String) null, null, null);
		openPage(Page.Landing);
		if (landingPage.landingStamp != null) landingPage.landingStamp.open(dialog);
	}

	public void openAbout() {
		set((String) null, null, null);
		openPage(Page.About);
		if (aboutPage.aboutStamp != null) aboutPage.aboutStamp.open();
	}

	public void openLanguage(String language, String tab) {
		boolean openTab = current != null && current == Page.Language;
		set(language, null, null);
		openPage(Page.Language);
		if (languagePage.languageStamp == null) return;
		if (openTab) languagePage.languageStamp.openTab(tab);
		else languagePage.languageStamp.open(language, tab);
	}

	public void openModel(String model, String release, String tab, String view, String file, String position) {
		Model modelInstance = box().modelManager().get(model);
		Language language = modelInstance != null ? box().languageManager().get(modelInstance) : null;
		set(language, release, modelInstance);
		openPage(Page.Model);
		if (modelPage.modelStamp != null) modelPage.modelStamp.open(model, release, tab, view, file, position);
	}

	public void openStartingModel(String model) {
		set(null, null, model);
		openPage(Page.Model);
		if (modelPage.modelStamp != null) modelPage.modelStamp.openStarting(model);
	}

	public void openTemplate(String languageKey, String version) {
		Language language = box().languageManager().get(languageKey);
		LanguageRelease release = language.release(version);
		openModel(release.template(), release.version(), null, null, null, null);
	}

	public void openHelp(String language, String version) {
		set(language, version, null);
		openPage(Page.Language);
		if (languagePage.languageStamp != null) languagePage.languageStamp.openHelp(language, version);
	}

	public void open(String model, String release, String view) {
		set(null, release, view);
		openPage(Page.Forge);
		if (forgePage.forgeStamp != null) forgePage.forgeStamp.open(model, release, view);
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
		header.release(release);
		header.model(model);
		header.refresh();
	}

	private void hidePages() {
		if (landingPage.isVisible()) landingPage.hide();
		if (aboutPage.isVisible()) aboutPage.hide();
		if (languagePage.isVisible()) languagePage.hide();
		if (modelPage.isVisible()) modelPage.hide();
		if (forgePage.isVisible()) forgePage.hide();
	}

	private BlockConditional<?, ?> blockOf(Page page) {
		if (page == Page.Landing) return landingPage;
		if (page == Page.About) return aboutPage;
		if (page == Page.Language) return languagePage;
		if (page == Page.Model) return modelPage;
		if (page == Page.Forge) return forgePage;
		return null;
	}

	private void set(String language, String release, String model) {
		Language languageInstance = language != null ? box().languageManager().get(language) : null;
		Model modelInstance = model != null ? box().modelManager().get(model) : null;
		set(languageInstance, release, modelInstance);
	}

	private void set(Language language, String release, Model model) {
		this.language = language;
		this.release = release;
		this.model = model;
	}

	private void registerUserIfNeeded() {
		User user = session().user();
		if (user == null) return;
		if (box().userManager().exists(user.username())) return;
		box().commands(UserCommands.class).create(user.username(), username());
	}

}