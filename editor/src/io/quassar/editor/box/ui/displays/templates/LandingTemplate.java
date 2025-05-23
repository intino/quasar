package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.ModelCommands;
import io.quassar.editor.box.ui.datasources.LanguagesWithReleaseDatasource;
import io.quassar.editor.box.ui.types.LandingDialog;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.model.GavCoordinates;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import io.quassar.editor.model.Model;

import java.util.List;
import java.util.Set;

public class LandingTemplate extends AbstractLandingTemplate<EditorBox> {
	private LandingDialog dialog;
	private boolean closing = false;

	public LandingTemplate(EditorBox box) {
		super(box);
	}

	public void open(String dialog) {
		this.dialog = dialog != null ? LandingDialog.from(dialog) : null;
		refresh();
	}

	@Override
	public void didMount() {
		super.didMount();
		content.cssSelectors(Set.of("landing-page"));
	}

	@Override
	public void init() {
		super.init();
		modelsDialog.onOpen(e -> refreshModelsDialog());
		modelsDialog.onClose(e -> notifyClose());
		languagesDialog.onOpen(e -> refreshLanguagesDialog());
		languagesDialog.onClose(e -> notifyClose());
		startModelingLogin.onExecute(e -> gotoLogin(PathHelper.landingUrl(LandingDialog.StartModeling, session())));
		//startBuildingLogin.onExecute(e -> gotoLogin(PathHelper.homeUrl(session())));
		//startBuilding.onExecute(e -> startBuilding());
	}

	@Override
	public void refresh() {
		super.refresh();
		refreshMainBlocks();
		refreshLanguagesCatalog();
		openDialogIfRequired();
	}

	private void refreshMainBlocks() {
		startModelingLogin.visible(user() == null);
		startModeling.visible(user() != null);
		if (startModeling.isVisible()) startModeling.address(path -> PathHelper.landingPath(path, LandingDialog.StartModeling));
		Language language = box().languageManager().get(Language.Metta);
		List<Model> models = box().modelManager().models(language);
		exploreLanguage.address(path -> PathHelper.languagePath(path, language, username() == null || models.isEmpty() ? LanguageTab.Examples : LanguageTab.Models));
		startBuilding.address(path -> PathHelper.languagePath(path, language, username() == null || models.isEmpty() ? LanguageTab.Examples : LanguageTab.Models));
		//startBuildingLogin.visible(user() == null);
		//startBuilding.visible(user() != null);
	}

	private void refreshLanguagesCatalog() {
		languagesCatalog.refresh();
	}

	private void openDialogIfRequired() {
		if (dialog == null) { closing = true; languagesDialog.close(); modelsDialog.close(); }
		if (dialog == LandingDialog.Languages) languagesDialog.open();
		else if (dialog == LandingDialog.Examples) modelsDialog.open();
		else if (dialog == LandingDialog.StartModeling) languagesDialog.open();
	}

	private void refreshModelsDialog() {
		closing = false;
		modelsDialog.title("Example Metta models");
		modelsStamp.language(box().languageManager().get(Language.key(Language.QuassarGroup, Language.Metta)));
		modelsStamp.tab(LanguageTab.Examples);
		modelsStamp.refresh();
	}

	private void refreshLanguagesDialog() {
		closing = false;
		languagesDialog.title(dialog == LandingDialog.StartModeling ? "Select the DSL to start modeling with" : "Explore DSLs of our community");
		languagesStamp.onSelect(dialog == LandingDialog.StartModeling ? this::startModeling : null);
		languagesStamp.source(new LanguagesWithReleaseDatasource(box(), session()));
		languagesStamp.refresh();
	}

	private void startModeling(Language language) {
		notifier.dispatch(PathHelper.languagePath(language));
	}

//	private void startModeling(Language language) {
//		String name = ModelHelper.proposeName();
//		LanguageRelease release = language.lastRelease();
//		Model model = box().commands(ModelCommands.class).create(name, name, translate("(no description)"), GavCoordinates.fromString(language, release), username(), username());
//		notifier.dispatch(PathHelper.startingModelPath(model));
//	}

	private void startBuilding() {
		startModeling(box().languageManager().get(Language.key(Language.QuassarGroup, Language.Metta)));
	}

	private void notifyClose() {
		if (closing) return;
		notifier.dispatch(PathHelper.homePath());
	}

	private void gotoLogin(String callback) {
		session().add("callback", callback);
		if (box().authService() == null) notifier.redirect(PathHelper.loginUrl(session()));
		else notifier.redirect(session().login(session().browser().baseUrl()));
	}

}