package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.server.UIFile;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command.ExecutionResult;
import io.quassar.editor.box.commands.ModelCommands;
import io.quassar.editor.box.util.*;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageTool;
import io.quassar.editor.model.Model;
import io.quassar.editor.model.Project;

import java.io.File;
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
	private BiConsumer<Model, ExecutionResult> deployListener;
	private List<LanguageTool> tools;

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

	public void onDeploy(BiConsumer<Model, ExecutionResult> listener) {
		this.deployListener = listener;
	}

	@Override
	public void init() {
		super.init();
		title.onExecute(e -> openTitleEditor());
		titleEditor.onEnterPress(e -> saveTitle());
		closeTitleEditor.onExecute(e -> closeTitleEditor());
		saveTitleEditor.onExecute(e -> saveTitle());
		releaseSelector.onExecute(e -> openRelease(e.option()));
		checkTrigger.onExecute(e -> check());
		commitTrigger.onExecute(e -> commit());
		cloneTrigger.onExecute(e -> cloneModel());
		downloadTrigger.onExecute(e -> download());
		commitModelDialog.onCommit((m, v) -> openRelease(v));
		commitModelDialog.onCommitFailure((m, v) -> deployListener.accept(m, v));
		infoTrigger.onExecute(e -> openSettingsDialog());
		modelSettingsDialog.onSave(e -> refresh());
		toolsTrigger.onExecute(e -> openTools());
	}

	@Override
	public void refresh() {
		super.refresh();
		contentBlock.visible(model != null);
		if (!contentBlock.isVisible()) return;
		Project project = box().projectManager().find(model);
		Language language = box().languageManager().get(model);
		projectModelSelector.readonly(project == null);
		title.readonly(model.isTemplate());
		title.title(ModelHelper.label(model, language(), box()));
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
		toolsTrigger.visible(!model.isTemplate() && release != null && !release.equals(Model.DraftRelease) && !model.language().artifactId().equals(Language.Metta));
		toolsTrigger.readonly(!PermissionsHelper.canOpenTools(model, language, release, session()));
		downloadTrigger.visible(ModelHelper.validReleaseName(release, this::translate));
		cloneTrigger.visible(!model.isTemplate());
		cloneTrigger.readonly(!PermissionsHelper.canClone(model, release, session(), box()));
		languageLogo.visible(language != null);
		if (languageLogo.isVisible()) languageLogo.value(LanguageHelper.logo(language, box()));
		languageHelpTrigger.visible(language != null);
		if (languageHelpTrigger.isVisible()) {
			languageHelpTrigger.title(LanguageHelper.title(model.language()));
			languageHelpTrigger.site(PathHelper.languageReleaseHelp(language, model.language().version()));
		}
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

	private UIFile download() {
		File release = box().modelManager().release(model, this.release);
		return DisplayHelper.uiFile(model.name() + "-" + release.getName(), release);
	}

	private void openSettingsDialog() {
		modelSettingsDialog.model(model);
		modelSettingsDialog.open();
	}

	private void openTitleEditor() {
		title.visible(false);
		titleEditor.value(title.title());
		titleEditorBlock.visible(true);
		titleEditor.focus();
	}

	private void closeTitleEditor() {
		title.visible(true);
		titleEditorBlock.visible(false);
	}

	private void saveTitle() {
		String value = titleEditor.value();
		box().commands(ModelCommands.class).save(model, value, username());
		closeTitleEditor();
		title.title(value);
	}

	private void openTools() {
		toolLauncher.model(model);
		toolLauncher.release(release);
		toolLauncher.launch();
	}

}