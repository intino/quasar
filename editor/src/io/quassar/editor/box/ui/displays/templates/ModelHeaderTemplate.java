package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.server.UIFile;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.commands.Command.ExecutionResult;
import io.quassar.editor.box.models.ModelContainer;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.model.Language;
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
	private BiConsumer<Model, ExecutionResult> publishFailureListener;

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

	public void onPublishFailure(BiConsumer<Model, ExecutionResult> listener) {
		this.publishFailureListener = listener;
	}

	@Override
	public void init() {
		super.init();
		releaseSelector.onExecute(e -> openRelease(e.option()));
		publishTrigger.onExecute(e -> publish());
		downloadTrigger.onExecute(e -> download());
		modelPublishDialog.onPublish((m, v) -> openRelease(v));
		modelPublishDialog.onPublishFailure((m, v) -> publishFailureListener.accept(m, v));
		modelSettingsTrigger.onExecute(e -> openSettingsDialog());
		modelSettingsDialog.onRename(e -> notifier.dispatch(PathHelper.modelPath(model)));
		modelSettingsDialog.onSave(e -> refresh());
	}

	@Override
	public void refresh() {
		super.refresh();
		contentBlock.visible(model != null);
		if (!contentBlock.isVisible()) return;
		Language language = box().languageManager().get(model.language());
		Project project = box().projectManager().find(model);
		projectModelSelector.readonly(project == null);
		title.value(ModelHelper.label(model, language(), box()));
		refreshReleaseSelector();
		publishTrigger.visible(release == null || release.equals(translate(Model.DraftRelease)));
		publishTrigger.readonly(!PermissionsHelper.canPublish(model, release, session(), box()));
		downloadTrigger.visible(ModelHelper.validReleaseName(release, this::translate));
		homeOperation.address(a -> PathHelper.languagePath(a, language, LanguageTab.Home));
		modelsOperation.address(a -> PathHelper.languagePath(a, language, LanguageTab.Models));
		languageTitle.value(language.name());
		languageLogo.value(box().archetype().languages().logo(language.name()));
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

}