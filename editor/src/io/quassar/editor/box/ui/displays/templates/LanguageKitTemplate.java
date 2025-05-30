package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.server.UIFile;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.commands.Command.CommandResult;
import io.quassar.editor.box.commands.LanguageCommands;
import io.quassar.editor.box.commands.ModelCommands;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import io.quassar.editor.model.Model;

import java.io.File;
import java.util.function.Consumer;

public class LanguageKitTemplate extends AbstractLanguageKitTemplate<EditorBox> {
	private Language language;
	private String release;
	private Consumer<CommandResult> createVersionListener;

	public LanguageKitTemplate(EditorBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void release(String release) {
		this.release = release;
	}

	public void onCreateVersion(Consumer<CommandResult> listener) {
		this.createVersionListener = listener;
	}

	@Override
	public void init() {
		super.init();
		createVersion.onExecute(e -> createVersion());
		createTemplate.onExecute(e -> createTemplate());
		modelsCatalog.onCreateModel(e -> createModel());
	}

	@Override
	public void refresh() {
		super.refresh();
		selectVersionBlock.visible(release == null);
		versionBlock.visible(release != null && release() != null);
		versionNotCreatedBlock.visible(release != null && release() == null);
		if (!versionBlock.isVisible()) return;
		refreshMetamodel();
		refreshDownloads();
		refreshMavenDependencies();
		refreshTemplate();
		refreshExamples();
	}

	private void refreshMetamodel() {
		Model metamodel = box().modelManager().get(language.metamodel());
		metamodelLink.title(ModelHelper.label(metamodel, language(), box()));
		metamodelLink.site(PathHelper.modelUrl(metamodel, release, session()));
	}

	private void refreshDownloads() {
		File graphFile = box().languageManager().loadGraph(language, release());
		downloadsBlock.visible(graphFile != null);
		if (!downloadsBlock.isVisible()) return;
		downloads.clear();
		fill(graphFile, downloads.add());
		//box().languageManager().loadReaders(language, release()).forEach(r -> fill(r, downloads.add()));
	}

	private void refreshMavenDependencies() {
		dependencies.clear();
		box().languageManager().loadReaders(language, release()).forEach(r -> fill(r, dependencies.add()));
	}

	private void refreshTemplate() {
		String templateId = release().template();
		Model template = templateId != null ? box().modelManager().get(templateId) : null;
		templateDefined.visible(template != null);
		templateNotDefined.visible(template == null);
		if (template == null) return;
		templateLink.title(ModelHelper.label(template, language(), box()));
		templateLink.site(PathHelper.modelUrl(template, Model.DraftRelease, session()));
		templateCreateDate.value(template.createDate());
	}

	private void refreshExamples() {
		modelsCatalog.language(language);
		modelsCatalog.mode(ModelsTemplate.Mode.Forge);
		modelsCatalog.release(release());
		modelsCatalog.tab(LanguageTab.Examples);
		modelsCatalog.refresh();
	}

	private void fill(File file, DownloadTemplate display) {
		display.language(language);
		display.release(release);
		display.file(file);
		display.refresh();
	}

	private void fill(File file, DependencyTemplate display) {
		display.language(language);
		display.release(release);
		display.file(file);
		display.refresh();
	}

	private void createVersion() {
		notifyUser("Creating version...", UserMessage.Type.Loading);
		createVersion.readonly(true);
		createVersionListener.accept(box().commands(LanguageCommands.class).createRelease(language, release, username()));
		createVersion.readonly(false);
		hideUserNotification();
	}

	private LanguageRelease release() {
		return language.release(release);
	}

	private void createTemplate() {
		box().commands(ModelCommands.class).createTemplate(language, release(), username());
		refreshTemplate();
	}

	private Model createModel() {
		Model result = box().commands(ModelCommands.class).createExample(language, release(), username());
		refreshExamples();
		return result;
	}

}