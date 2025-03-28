package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.server.UIFile;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command.ExecutionResult;
import io.quassar.editor.box.commands.ModelCommands;
import io.quassar.editor.box.models.ModelContainer;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.model.Model;
import io.quassar.editor.model.Project;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ModelHeaderTemplate extends AbstractModelHeaderTemplate<EditorBox> {
	private Model model;
	private String release;
	private ModelContainer.File file;
	private BiConsumer<Model, ExecutionResult> buildListener;
	private BiConsumer<Model, ExecutionResult> publishListener;

	public ModelHeaderTemplate(EditorBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
	}

	public void release(String value) {
		this.release = value;
	}

	public void file(ModelContainer.File value) {
		this.file = value;
	}

	public void onBuild(BiConsumer<Model, ExecutionResult> listener) {
		this.buildListener = listener;
	}

	public void onPublish(BiConsumer<Model, ExecutionResult> listener) {
		this.publishListener = listener;
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
		publishTrigger.onExecute(e -> publish());
		downloadTrigger.onExecute(e -> download());
		modelPublishDialog.onPublish((m, v) -> openRelease(v));
		modelPublishDialog.onPublishFailure((m, v) -> publishListener.accept(m, v));
		modelSettingsTrigger.onExecute(e -> openSettingsDialog());
		modelSettingsDialog.onRename(e -> notifier.dispatch(PathHelper.modelPath(model)));
		modelSettingsDialog.onSave(e -> refresh());
	}

	@Override
	public void refresh() {
		super.refresh();
		contentBlock.visible(model != null);
		if (!contentBlock.isVisible()) return;
		Project project = box().projectManager().find(model);
		projectModelSelector.readonly(project == null);
		title.title(ModelHelper.label(model, language(), box()));
		refreshReleaseSelector();
		buildTrigger.visible(release == null || release.equals(translate(Model.DraftRelease)));
		buildTrigger.readonly(!PermissionsHelper.canBuild(model, release, session(), box()));
		publishTrigger.visible(release == null || release.equals(translate(Model.DraftRelease)));
		publishTrigger.readonly(!PermissionsHelper.canPublish(model, release, session(), box()));
		downloadTrigger.visible(ModelHelper.validReleaseName(release, this::translate));
	}

	private void refreshReleaseSelector() {
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
		notifyUser(translate("Building model..."), UserMessage.Type.Loading);
		ExecutionResult result = box().commands(ModelCommands.class).build(model, username());
		buildListener.accept(model, result);
		if (result.success()) notifyUser("Model built successfully", UserMessage.Type.Success);
		else hideUserNotification();
	}

	private void publish() {
		modelPublishDialog.model(model);
		modelPublishDialog.open();
	}

	private UIFile download() {
		File release = box().modelManager().release(model, this.release);
		return new UIFile() {
			@Override
			public String label() {
				return model.name() + " " + release.getName();
			}

			@Override
			public InputStream content() {
				try {
					return new FileInputStream(release);
				} catch (FileNotFoundException e) {
					Logger.error(e);
					return new ByteArrayInputStream(new byte[0]);
				}
			}
		};
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