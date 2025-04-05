package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.server.UIFile;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command.ExecutionResult;
import io.quassar.editor.box.commands.ModelCommands;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.box.util.PermissionsHelper;
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
	private Consumer<Model> buildListener;
	private Consumer<Model> helpListener;
	private Consumer<Model> cloneListener;
	private BiConsumer<Model, ExecutionResult> deployListener;
	private Consumer<Model> editTemplateListener;

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

	public void onEditTemplate(Consumer<Model> listener) {
		this.editTemplateListener = listener;
	}

	public void onBuild(Consumer<Model> listener) {
		this.buildListener = listener;
	}

	public void onHelp(Consumer<Model> listener) {
		this.helpListener = listener;
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
		buildTrigger.onExecute(e -> build());
		deployTrigger.onExecute(e -> deploy());
		cloneTrigger.onExecute(e -> cloneModel());
		downloadTrigger.onExecute(e -> download(e.option()));
		modelDeployDialog.onDeploy((m, v) -> openRelease(v));
		modelDeployDialog.onDeployFailure((m, v) -> deployListener.accept(m, v));
		modelSettingsTrigger.onExecute(e -> openSettingsDialog());
		modelSettingsDialog.onRename(e -> notifier.dispatch(PathHelper.modelPath(model)));
		modelSettingsDialog.onSave(e -> refresh());
		languageHelpTrigger.onExecute(e -> helpListener.accept(model));
	}

	@Override
	public void refresh() {
		super.refresh();
		contentBlock.visible(model != null);
		if (!contentBlock.isVisible()) return;
		Project project = box().projectManager().find(model);
		projectModelSelector.readonly(project == null);
		title.readonly(model.isTemplate());
		title.title(ModelHelper.label(model, language(), box()));
		refreshReleaseSelector();
		buildTrigger.visible(release == null || release.equals(translate(Model.DraftRelease)));
		buildTrigger.readonly(!PermissionsHelper.canBuild(model, release, session(), box()));
		deployTrigger.visible(!model.isTemplate() && (release == null || release.equals(translate(Model.DraftRelease))));
		deployTrigger.readonly(!PermissionsHelper.canDeploy(model, release, session(), box()));
		cloneTrigger.visible(!model.isTemplate());
		cloneTrigger.readonly(!PermissionsHelper.canClone(model, release, session(), box()));
		editTemplateTrigger.visible(editTemplateListener != null);
		editTemplateTrigger.site(PathHelper.modelTemplateUrl(model, session()));
		editTemplateTrigger.readonly(!PermissionsHelper.canEditTemplate(model, release, session(), box()));
		openLanguageTrigger.visible(box().languageManager().exists(model.name()));
		if (openLanguageTrigger.isVisible()) openLanguageTrigger.address(path -> PathHelper.languagePath(path, model.name()));
		modelSettingsTrigger.visible(!model.isTemplate());
		modelSettingsTrigger.readonly(!PermissionsHelper.canEditSettings(model, release, session()));
		downloadTrigger.visible(ModelHelper.validReleaseName(release, this::translate));
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

	private void build() {
		buildListener.accept(model);
	}

	private void deploy() {
		modelDeployDialog.model(model);
		modelDeployDialog.open();
	}

	private void cloneModel() {
		cloneListener.accept(model);
	}

	private UIFile download(String option) {
		if (option.equals("Accessor")) return downloadAccessor();
		return downloadModel();
	}

	private UIFile downloadModel() {
		File release = box().modelManager().release(model, this.release);
		return DisplayHelper.uiFile(model.name() + "-" + release.getName(), release);
	}

	private UIFile downloadAccessor() {
		File release = box().modelManager().releaseAccessor(model, this.release);
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

}