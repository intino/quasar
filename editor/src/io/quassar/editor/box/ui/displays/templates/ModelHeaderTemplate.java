package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command.CommandResult;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ModelHeaderTemplate extends AbstractModelHeaderTemplate<EditorBox> {
	private Model model;
	private String release;
	private io.quassar.editor.box.models.File file;
	private Consumer<Model> checkListener;
	private Consumer<Model> cloneListener;
	private BiConsumer<Model, CommandResult> deployListener;
	private Consumer<Model> updateLanguageVersionListener;

	public ModelHeaderTemplate(EditorBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
	}

	public void release(String value) {
		this.release = value;
	}

	public void file(io.quassar.editor.box.models.File value) {
		this.file = value;
	}

	public void onCheck(Consumer<Model> listener) {
		this.checkListener = listener;
	}

	public void onClone(Consumer<Model> listener) {
		this.cloneListener = listener;
	}

	public void onDeploy(BiConsumer<Model, CommandResult> listener) {
		this.deployListener = listener;
	}

	public void onUpdateLanguageVersion(Consumer<Model> listener) {
		this.updateLanguageVersionListener = listener;
	}

	public void openInfo() {
		openSettingsDialog();
	}

	@Override
	public void init() {
		super.init();
		releaseSelector.onExecute(e -> openRelease(e.option()));
		checkTrigger.onExecute(e -> check());
		commitTrigger.onExecute(e -> commit());
		downloadTrigger.onExecute(e -> openDownloadDialog());
		commitModelDialog.onCommit((m, v) -> openRelease(v));
		commitModelDialog.onCommitFailure((m, v) -> deployListener.accept(m, v));
		commitModelDialog.onCreateRelease((m, v) -> deployListener.accept(m, v));
		infoTrigger.onExecute(e -> openSettingsDialog());
		modelSettingsDialog.onSave(e -> refresh());
		modelSettingsDialog.onClone(e -> cloneModel());
		modelSettingsDialog.onUpdateLanguageVersion(e -> notifyUpdateLanguageVersion());
		executionTrigger.onExecute(e -> openExecutionLauncher());
	}

	@Override
	public void refresh() {
		super.refresh();
		contentBlock.visible(model != null);
		if (!contentBlock.isVisible()) return;
		Language language = box().languageManager().get(model);
		titleViewer.model(model);
		titleViewer.refresh();
		refreshReleaseSelector();
		checkTrigger.visible(release == null || release.equals(translate(Model.DraftRelease)));
		checkTrigger.readonly(!PermissionsHelper.canCheck(model, release, session(), box()));
		commitTrigger.visible(!model.isTemplate() && (release == null || release.equals(translate(Model.DraftRelease))));
		commitTrigger.readonly(!PermissionsHelper.canCommit(model, release, session(), box()));
		infoTrigger.visible(!model.isTemplate());
		infoTrigger.readonly(!PermissionsHelper.canEditSettings(model, release, session()));
		forgeTrigger.visible(!model.isTemplate() && release != null && !release.equals(Model.DraftRelease) && model.language().artifactId().equals(Language.Metta));
		forgeTrigger.readonly(!PermissionsHelper.canForge(model, language, release, session()));
		if (forgeTrigger.isVisible()) forgeTrigger.site(PathHelper.forgeUrl(model, release, session()));
		executionTrigger.visible(!model.isTemplate() && release != null && !release.equals(Model.DraftRelease) && !model.language().artifactId().equals(Language.Metta));
		executionTrigger.readonly(!PermissionsHelper.canLaunchExecution(model, language, release, session()));
		downloadTrigger.visible(ModelHelper.validReleaseName(release, this::translate));
		languageLogo.visible(language != null);
		if (languageLogo.isVisible()) languageLogo.value(LanguageHelper.logo(language, box()));
		helpTrigger.visible(language != null);
		languageTrigger.visible(language != null);
		if (language == null) return;
		helpTrigger.title(LanguageHelper.title(model.language()));
		helpTrigger.site(PathHelper.languageReleaseHelp(language, model.language().version()));
		languageTrigger.title(translate("Goto %s").formatted(LanguageHelper.title(model.language())));
		languageTrigger.address(path -> PathHelper.languagePath(path, language));
	}

	private void refreshReleaseSelector() {
		releaseSelector.visible(!model.isTemplate());
		if (!releaseSelector.isVisible()) return;
		releaseSelector.clear();
		List<String> options = new ArrayList<>(box().modelManager().releases(model)).reversed();
		options.addFirst(translate(Model.DraftRelease));
		releaseSelector.options(options);
		releaseSelector.option(release);
	}

	private void openRelease(String release) {
		String releaseName = release.equals(translate(Model.DraftRelease)) ? Model.DraftRelease : release;
		notifier.dispatch(PathHelper.modelPath(model, releaseName, file));
	}

	private void check() {
		checkListener.accept(model);
	}

	private void commit() {
		commitModelDialog.model(model);
		commitModelDialog.open();
	}

	private void cloneModel() {
		cloneListener.accept(model);
	}

	private void openDownloadDialog() {
		downloadModelDialog.model(model);
		downloadModelDialog.release(release);
		downloadModelDialog.open();
	}

	private void openSettingsDialog() {
		modelSettingsDialog.model(model);
		modelSettingsDialog.release(release);
		modelSettingsDialog.open();
	}

	private void openExecutionLauncher() {
		executionLauncher.model(model);
		executionLauncher.release(release);
		executionLauncher.launch();
	}

	private void notifyUpdateLanguageVersion() {
		updateLanguageVersionListener.accept(model);
		refresh();
	}

}