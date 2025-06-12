package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command.CommandResult;
import io.quassar.editor.box.commands.LanguageCommands;
import io.quassar.editor.box.commands.ModelCommands;
import io.quassar.editor.box.ui.displays.HelpEditor;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import io.quassar.editor.model.Model;

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

	public void notifyRemove(Model model) {
		modelsCatalog.refresh();
	}

	public void notifyChange(Model model) {
		if (!model.isExample()) return;
		if (model.language().languageId().equals(language.name())) return;
		modelsCatalog.refresh(model);
	}

	@Override
	public void init() {
		super.init();
		createVersion.onExecute(e -> createVersion());
		createTemplate.onExecute(e -> createTemplate());
		helpDialog.onOpen(e -> refreshHelpDialog());
		examplesDialog.onOpen(e -> refreshExamplesDialog());
		modelsCatalog.onCreateModel(e -> createModel());
		startModeling.onExecute(e -> startModeling());
	}

	@Override
	public void refresh() {
		super.refresh();
		boolean hasCommits = hasCommits();
		selectVersionBlock.visible(release == null && language != null);
		versionBlock.visible(release != null && release() != null);
		versionNotCreatedBlock.visible(release != null && release() == null && hasCommits);
		refreshNoVersionsBlock(hasCommits);
		if (!versionBlock.isVisible()) return;
		refreshTemplate();
	}

	private boolean hasCommits() {
		if (language == null) return false;
		Model metamodel = box().modelManager().get(language.metamodel());
		return metamodel != null && !metamodel.releases().isEmpty();
	}

	private void refreshNoVersionsBlock(boolean hasCommits) {
		noVersionsBlock.visible(language != null && !hasCommits);
		if (!noVersionsBlock.isVisible()) return;
		Model metamodel = box().modelManager().get(language.metamodel());
		metamodelLink.site(PathHelper.modelUrl(metamodel, session()));
	}

	private void refreshTemplate() {
		String templateId = release().template();
		Model template = templateId != null ? box().modelManager().get(templateId) : null;
		templateDefined.visible(template != null);
		templateNotDefined.visible(template == null);
		if (template == null) return;
		templateLink.site(PathHelper.modelUrl(template, Model.DraftRelease, session()));
	}

	private void refreshExamples() {
		modelsCatalog.language(language);
		modelsCatalog.mode(ModelsTemplate.Mode.Forge);
		modelsCatalog.release(release());
		modelsCatalog.tab(LanguageTab.Examples);
		modelsCatalog.bindTo(modelsDialog);
		modelsCatalog.refresh();
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

	private void refreshHelpDialog() {
		helpDialog.title(translate("Edit help for %s release").formatted(release));
		helpEditor.clear();
		HelpEditor display = new HelpEditor(box());
		helpEditor.display(display);
		display.language(language);
		display.release(release);
		display.refresh();
	}

	private void startModeling() {
		openModel.site(PathHelper.languageUrl(language, session()));
		openModel.launch();
	}

	private void refreshExamplesDialog() {
		examplesDialog.title(translate("Examples of %s %s").formatted(language.name(), release));
		refreshExamples();
	}

}