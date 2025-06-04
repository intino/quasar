package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.components.Actionable;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command.CommandResult;
import io.quassar.editor.box.ui.types.ModelView;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import io.quassar.editor.model.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ModelHeaderTemplate extends AbstractModelHeaderTemplate<EditorBox> {
	private Model model;
	private String release;
	private ModelView view;
	private io.quassar.editor.box.models.File file;
	private Function<Model, Boolean> checkListener;
	private Consumer<Model> cloneListener;
	private BiConsumer<Model, CommandResult> deployListener;
	private Step step = Step.Edit;

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

	public void file(io.quassar.editor.box.models.File value) {
		this.file = value;
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
		step = !Model.DraftRelease.equals(release) ? Step.Commit : Step.Edit;
		titleViewer.model(model);
		titleViewer.refresh();
		refreshReleaseSelector();
		refreshToolbar();
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

	private void refreshToolbar() {
		Language language = box().languageManager().get(model);
		Language forgedLanguage = box().languageManager().getWithMetamodel(model);
		editTrigger.highlight(step == Step.Edit ? Actionable.Highlight.Fill : Actionable.Highlight.Outline);
		editTrigger.visible(!model.isTemplate() && !model.isExample());
		checkTrigger.readonly(!PermissionsHelper.canCheck(model, release, session(), box()));
		checkTrigger.visible(PermissionsHelper.isOwnerOrCollaborator(model, session(), box()));
		checkTrigger.highlight(step == Step.Check && !model.isTemplate() && !model.isExample() ? Actionable.Highlight.Fill : Actionable.Highlight.Outline);
		checkTrigger.title(model.isTemplate() || model.isExample() ? translate("Check") : translate("2. Check"));
		commitTrigger.visible(!model.isTemplate() && !model.isExample());
		commitTrigger.readonly(step == Step.Edit || !PermissionsHelper.canCommit(model, release, session(), box()));
		commitTrigger.highlight(step == Step.Commit ? Actionable.Highlight.Fill : Actionable.Highlight.Outline);
		forgeTrigger.visible(!model.isTemplate() && release != null && model.language().artifactId().equals(Language.Metta));
		forgeTrigger.readonly(step == Step.Edit || release == null || release.equals(Model.DraftRelease) || !PermissionsHelper.canForge(model, language, release, session(), box()));
		forgeTrigger.highlight(step == Step.Forge ? Actionable.Highlight.Fill : Actionable.Highlight.Outline);
		if (forgeTrigger.isVisible()) forgeTrigger.site(PathHelper.forgeUrl(model, release, session()));
		executionTrigger.visible(!model.isTemplate() && !model.isExample() && ModelHelper.isM1Release(model, release));
		if (executionTrigger.isVisible()) executionTrigger.title(executionName());
		executionTrigger.readonly(step == Step.Edit || release == null || release.equals(Model.DraftRelease) || !PermissionsHelper.canLaunchExecution(model, language, release, session(), box()));
		executionTrigger.highlight(step == Step.Forge ? Actionable.Highlight.Fill : Actionable.Highlight.Outline);
		downloadTrigger.visible(ModelHelper.validReleaseName(release, this::translate));
		cloneTrigger.visible(session().user() != null && model.isExample());
		noLanguageDefinedBlock.visible(!model.isTemplate() && model.language().artifactId().equals(Language.Metta) && (forgedLanguage == null || model.releases().isEmpty()));
		languageDefinedBlock.visible(forgedLanguage != null && !model.isTemplate() && model.language().artifactId().equals(Language.Metta) && !model.releases().isEmpty());
		if (languageDefinedBlock.isVisible()) {
			gotoForgeTrigger.site(PathHelper.forgeUrl(model, release, session()));
			gotoForgeTrigger.title(forgedLanguage.name() + " DSL");
		}
		releaseSelector.visible(!model.isTemplate() && !model.isExample() && !model.releases().isEmpty());
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
		notifier.dispatch(PathHelper.modelPath(model, releaseName, view, file));
	}

	private void edit() {
		openRelease(Model.DraftRelease);
		refreshToolbar();
	}

	private void check() {
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

	private static final String DefaultExecutionName = "4. forge";
	private String executionName() {
		Language language = box().languageManager().get(model.language());
		if (language == null) return translate(DefaultExecutionName);
		LanguageRelease languageRelease = language.release(model.language().version());
		if (languageRelease == null) return translate(DefaultExecutionName);
		if (languageRelease.execution() == null) return translate(DefaultExecutionName);
		String name = languageRelease.execution().name();
		return translate(!name.isEmpty() ? "4. " + name : DefaultExecutionName);
	}

	private void updateStep(Step step) {
		this.step = step;
		refreshToolbar();
	}

}