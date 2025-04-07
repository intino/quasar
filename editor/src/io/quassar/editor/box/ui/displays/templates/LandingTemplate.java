package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.ModelCommands;
import io.quassar.editor.box.ui.types.LandingDialog;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

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
		startBuildingLogin.onExecute(e -> gotoLogin(PathHelper.homeUrl(session())));
		startBuilding.onExecute(e -> startBuilding());
	}

	@Override
	public void refresh() {
		super.refresh();
		startModelingLogin.visible(user() == null);
		startModeling.visible(user() != null);
		if (startModeling.isVisible()) startModeling.address(path -> PathHelper.landingPath(path, LandingDialog.StartModeling));
		exploreLanguages.address(path -> PathHelper.landingPath(path, LandingDialog.Languages));
		exploreModels.address(path -> PathHelper.landingPath(path, LandingDialog.Models));
		startBuildingLogin.visible(user() == null);
		startBuilding.visible(user() != null);
		openDialogIfRequired();
	}

	private void openDialogIfRequired() {
		if (dialog == null) { closing = true; languagesDialog.close(); modelsDialog.close(); }
		if (dialog == LandingDialog.Languages) languagesDialog.open();
		else if (dialog == LandingDialog.Models) modelsDialog.open();
		else if (dialog == LandingDialog.StartModeling) languagesDialog.open();
	}

	private void refreshModelsDialog() {
		closing = false;
		modelsDialog.title("List of available models");
		modelsStamp.language(box().languageManager().get(Language.Meta));
		modelsStamp.refresh();
	}

	private void refreshLanguagesDialog() {
		closing = false;
		languagesDialog.title(dialog == LandingDialog.StartModeling ? "Select the language to start modeling with" : "Explore DSLs of our community");
		languagesStamp.embedded(true);
		languagesStamp.onSelect(dialog == LandingDialog.StartModeling ? this::startModeling : null);
		languagesStamp.refresh();
	}

	private void startModeling(Language language) {
		String name = ModelHelper.proposeName();
		Model model = box().commands(ModelCommands.class).create(name, name, translate("(no hint)"), translate("(no description)"), language, username(), username());
		notifier.dispatch(PathHelper.startingModelPath(model));
	}

	private void startBuilding() {
		startModeling(box().languageManager().get(Language.Meta));
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