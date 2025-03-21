package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.server.UIFile;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.LanguageCommands;
import io.quassar.editor.box.commands.ModelCommands;
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

import static io.quassar.editor.box.util.ModelHelper.DraftVersion;

public class ModelHeaderTemplate extends AbstractModelHeaderTemplate<EditorBox> {
	private Model model;
	private String version;

	public ModelHeaderTemplate(EditorBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
	}

	public void version(String version) {
		this.version = version;
	}

	@Override
	public void init() {
		super.init();
		versionSelector.onExecute(e -> openVersion(e.option()));
		publishTrigger.onExecute(e -> publish());
		downloadTrigger.onExecute(e -> download());
		modelPublishDialog.onPublish((l, v) -> openVersion(v));
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
		refreshVersionSelector();
		publishTrigger.visible(version == null || version.equals(translate(DraftVersion)));
		publishTrigger.readonly(!PermissionsHelper.canPublish(model, session(), box()));
		downloadTrigger.visible(version != null && !version.equals(translate(DraftVersion)));
		homeOperation.address(a -> PathHelper.languagePath(a, language, LanguageTab.Home));
		modelsOperation.address(a -> PathHelper.languagePath(a, language, LanguageTab.Models));
		languageTitle.value(language.name());
		languageLogo.value(box().archetype().languages().logo(language.name()));
	}

	private void refreshVersionSelector() {
		versionSelector.clear();
		List<String> options = new ArrayList<>(box().modelManager().versions(model)).reversed();
		options.addFirst(translate(DraftVersion));
		versionSelector.options(options);
		versionSelector.option(version);
	}

	private void openVersion(String version) {
		String versionName = version.equals(translate(DraftVersion)) ? DraftVersion : version;
		notifier.dispatch(PathHelper.modelPath(model, versionName));
	}

	private void publish() {
		modelPublishDialog.model(model);
		modelPublishDialog.open();
	}

	private UIFile download() {
		File version = box().modelManager().version(model, this.version);
		return new UIFile() {
			@Override
			public String label() {
				return model.name() + " " + version.getName();
			}

			@Override
			public InputStream content() {
				try {
					return new FileInputStream(version);
				} catch (FileNotFoundException e) {
					Logger.error(e);
					return new ByteArrayInputStream(new byte[0]);
				}
			}
		};
	}

}