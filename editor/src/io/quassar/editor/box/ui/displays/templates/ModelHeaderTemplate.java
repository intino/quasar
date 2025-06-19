package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.components.Actionable;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command.CommandResult;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.ui.types.ModelView;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ModelHeaderTemplate extends AbstractModelHeaderTemplate<EditorBox> {
	private Model model;
	private String release;
	private ModelView view;
	private LanguageTab tab;
	private io.quassar.editor.box.models.File file;
	private FilePosition filePosition;
	private Function<Model, Boolean> checkListener;
	private Consumer<Model> openSettingsListener;
	private Consumer<Model> cloneListener;
	private BiConsumer<Model, CommandResult> deployListener;
	private Step step = Step.Edit;
	private boolean checked = false;

	private enum Step { Edit, Check, Commit, Forge }

	public ModelHeaderTemplate(EditorBox box) {
		super(box);
	}

	public Model model() {
		return this.model;
	}

	public void model(Model model) {
		this.model = model;
	}

	public void release(String value) {
		this.release = value;
	}

	public void view(ModelView value) {
		this.view = value;
	}

	public void tab(LanguageTab tab) {
		this.tab = tab;
	}

	public void file(io.quassar.editor.box.models.File value) {
		this.file = value;
	}

	public void position(FilePosition position) {
		this.filePosition = position;
	}

	public void onOpenSettings(Consumer<Model> listener) {
		this.openSettingsListener = listener;
	}

	public void onCheck(Function<Model, Boolean> listener) {
		this.checkListener = listener;
	}

	public void onClone(Consumer<Model> listener) {
		this.cloneListener = listener;
	}

	public void onDeploy(BiConsumer<Model, CommandResult> listener) {
		this.deployListener = listener;
	}

	public void checked(boolean value) {
		updateStep(value ? Step.Check : Step.Edit);
	}

	@Override
	public void init() {
		super.init();
		releaseSelector.onExecute(e -> openRelease(e.option()));
		settingsTrigger.onExecute(e -> openSettingsListener.accept(model));
		editTrigger.onExecute(e -> edit());
		checkTrigger.onExecute(e -> check());
		commitTrigger.onExecute(e -> commit());
		downloadTrigger.onExecute(e -> openDownloadDialog());
		forgeTrigger.onOpen(e -> updateStep(Step.Forge));
		cloneTrigger.onExecute(e -> cloneModel());
		commitModelDialog.onCommit((m, v) -> openRelease(v));
		commitModelDialog.onCommitFailure((m, v) -> deployListener.accept(m, v));
		commitModelDialog.onCreateRelease((m, v) -> deployListener.accept(m, v));
		executionTrigger.onExecute(e -> openExecutionLauncher());
	}

	@Override
	public void refresh() {
		super.refresh();
		contentBlock.visible(model != null);
		if (!contentBlock.isVisible()) return;
		Language language = box().languageManager().get(model);
		step = !Model.DraftRelease.equals(release) ? Step.Commit : (checked ? Step.Check : Step.Edit);
		checked = false;
		titleViewer.model(model);
		titleViewer.refresh();
		refreshReleaseSelector();
		refreshToolbar();
		refreshLanguageTrigger(language);
		refreshCloseTrigger(language);
	}

	private void refreshLanguageTrigger(Language language) {
		languageTrigger.visible(language != null && !model.isTemplate() && !model.isExample()/* && !PathHelper.comesFromForge(session())*/);
		if (!languageTrigger.isVisible() || language == null) return;
		languageTrigger.title(translate("Goto %s").formatted(LanguageHelper.title(model.language())));
		languageTrigger.address(path -> PathHelper.languagePath(path, language));
	}

	private void refreshCloseTrigger(Language language) {
		closeTrigger.visible(language != null && (model.isTemplate() || model.isExample())/* && PathHelper.comesFromForge(session())*/);
		if (!closeTrigger.isVisible()) return;
		closeTrigger.title(translate(model.isTemplate() ? "Template" : "Example"));
	}

	private void refreshToolbar() {
		Language language = box().languageManager().get(model);
		Language forgedLanguage = box().languageManager().getWithMetamodel(model);
		boolean hasValidLicense = PermissionsHelper.hasValidLicense(model.language(), session(), box());
		settingsTrigger.visible(PermissionsHelper.canEditSettings(model, release, session(), box()) && hasValidLicense);
		editTrigger.highlight(step == Step.Edit ? Actionable.Highlight.Fill : Actionable.Highlight.Outline);
		editTrigger.visible(!model.isTemplate() && !model.isExample() && hasValidLicense);
		checkTrigger.readonly(!PermissionsHelper.canCheck(model, release, session(), box()));
		checkTrigger.visible(PermissionsHelper.isOwnerOrCollaborator(model, session(), box()) && hasValidLicense);
		checkTrigger.highlight(step == Step.Check && !model.isTemplate() && !model.isExample() ? Actionable.Highlight.Fill : Actionable.Highlight.Outline);
		checkTrigger.title(model.isTemplate() || model.isExample() ? translate("Check") : translate("2. Check"));
		commitTrigger.visible(!model.isTemplate() && !model.isExample() && hasValidLicense);
		commitTrigger.readonly(step == Step.Edit || !PermissionsHelper.canCommit(model, release, session(), box()));
		commitTrigger.highlight(step == Step.Commit ? Actionable.Highlight.Fill : Actionable.Highlight.Outline);
		forgeTrigger.visible(!model.isTemplate() && release != null && model.language().artifactId().equals(Language.Metta));
		forgeTrigger.readonly(step == Step.Edit || release == null || release.equals(Model.DraftRelease) || !PermissionsHelper.canForge(model, language, release, session(), box()));
		forgeTrigger.highlight(step == Step.Forge ? Actionable.Highlight.Fill : Actionable.Highlight.Outline);
		if (forgeTrigger.isVisible()) forgeTrigger.site(PathHelper.forgeUrl(model, release, session()));
		executionTrigger.visible(!model.isTemplate() && !model.isExample() && ModelHelper.isM1Release(model, release) && executionDefined());
		if (executionTrigger.isVisible()) executionTrigger.title(executionName());
		executionTrigger.readonly(step == Step.Edit || release == null || release.equals(Model.DraftRelease) || !PermissionsHelper.canLaunchExecution(model, language, release, session(), box()));
		executionTrigger.highlight(step == Step.Forge ? Actionable.Highlight.Fill : Actionable.Highlight.Outline);
		downloadTrigger.visible(ModelHelper.validReleaseName(release, this::translate) && !executionDefined() && !model.language().artifactId().equals(Language.Metta));
		downloadTrigger.highlight(step == Step.Forge ? Actionable.Highlight.Fill : Actionable.Highlight.Outline);
		cloneTrigger.visible(session().user() != null && model.isExample() && !PermissionsHelper.isOwnerOrCollaborator(model, session(), box()));
		noLanguageDefinedBlock.visible(!model.isTemplate() && model.language().artifactId().equals(Language.Metta) && (forgedLanguage == null || model.releases().isEmpty()));
		languageDefinedBlock.visible(forgedLanguage != null && !model.isTemplate() && model.language().artifactId().equals(Language.Metta) && !model.releases().isEmpty());
		if (languageDefinedBlock.isVisible() && forgedLanguage != null) {
			gotoForgeTrigger.site(PathHelper.forgeUrl(model, release, session()));
			gotoForgeTrigger.title("%s DSL".formatted(forgedLanguage.name()));
		}
		releaseSelector.visible(!model.isTemplate() && !model.isExample() && !model.releases().isEmpty());
		languageToolbar.model(model);
		languageToolbar.release(release);
		languageToolbar.view(view);
		languageToolbar.tab(tab);
		languageToolbar.file(file);
		languageToolbar.filePosition(filePosition);
		languageToolbar.refresh();
	}

	private void refreshReleaseSelector() {
		releaseSelector.visible(!model.isTemplate());
		if (!releaseSelector.isVisible()) return;
		releaseSelector.clear();
		List<String> options = new ArrayList<>(box().modelManager().releases(model)).reversed();
		releaseSelector.options(options);
		releaseSelector.option(release != null ? release : null);
	}

	private void openRelease(String release) {
		String releaseName = release.equals(translate(Model.DraftRelease)) ? Model.DraftRelease : release;
		notifier.dispatch(PathHelper.modelPath(model, releaseName, tab, view, file));
	}

	private void edit() {
		openRelease(Model.DraftRelease);
		refreshToolbar();
	}

	private void check() {
		if (!Model.DraftRelease.equals(release)) {
			openRelease(Model.DraftRelease);
			checked = true;
		}
		updateStep(checkListener.apply(model) ? Step.Check : Step.Edit);
	}

	private void commit() {
		commitModelDialog.model(model);
		commitModelDialog.open();
		updateStep(Step.Commit);
	}

	private void cloneModel() {
		cloneListener.accept(model);
	}

	private void openDownloadDialog() {
		updateStep(Step.Forge);
		downloadModelDialog.model(model);
		downloadModelDialog.release(release);
		downloadModelDialog.open();
	}

	private void openExecutionLauncher() {
		executionLauncher.model(model);
		executionLauncher.release(release);
		executionLauncher.launch();
		updateStep(Step.Forge);
	}

	private static final String NoExecutionName = "4. inspect";
	private static final String DefaultExecutionName = "4. execute";
	private String executionName() {
		Language language = box().languageManager().get(model.language());
		if (language == null) return translate(DefaultExecutionName);
		LanguageRelease languageRelease = language.release(model.language().version());
		if (languageRelease == null) return translate(DefaultExecutionName);
		if (languageRelease.execution() == null || languageRelease.execution().type() == LanguageExecution.Type.None) return translate(NoExecutionName);
		String name = languageRelease.execution().name();
		return translate(!name.isEmpty() ? "4. " + name : DefaultExecutionName);
	}

	private boolean executionDefined() {
		Language language = box().languageManager().get(model.language());
		if (language == null) return false;
		LanguageRelease languageRelease = language.release(model.language().version());
		if (languageRelease == null) return false;
		return languageRelease.execution() != null && languageRelease.execution().type() != LanguageExecution.Type.None && !languageRelease.execution().content().isEmpty();
	}

	private void updateStep(Step step) {
		this.step = step;
		refreshToolbar();
	}

}