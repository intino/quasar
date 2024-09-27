package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.utils.DelayerUtil;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ImeBrowser;
import io.intino.ime.box.commands.LanguageCommands;
import io.intino.ime.box.commands.ModelCommands;
import io.intino.ime.box.models.ModelManager;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.ui.datasources.MemoryModelsDatasource;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.model.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class ModelHeaderTemplate extends AbstractModelHeaderTemplate<ImeBox> {
	private Model model;
	private String _title;
	private Consumer<Model> openModelListener;

	public ModelHeaderTemplate(ImeBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
	}

	public void title(String title) {
		this._title = title;
	}

	public void onOpenModel(Consumer<Model> listener) {
		this.openModelListener = listener;
	}

	@Override
	public void init() {
		super.init();
		cancel.onExecute(e -> hideTitleEditor());
		save.onExecute(e -> saveLabel());
		titleField.onEnterPress(e -> saveLabel());
		user.visible(session().user() != null);
		cloneModelEditor.onClone(this::open);
		login.onExecute(e -> {
			session().add("callback", session().browser().requestUrl());
			notifier.redirect(session().login(session().browser().baseUrl()));
		});
		publishDialog.onOpen(e -> refreshPublishDialog());
		publish.onExecute(e -> publish());
		build.onExecute(e -> build());
		modelChildrenCatalog.onOpenModel(e -> openModelListener.accept(e));
	}

	@Override
	public void refresh() {
		super.refresh();
		Release release = box().languageManager().lastRelease(model.modelingLanguage());
		refreshModelsLinks();
		login.visible(user() == null);
		titleLink.title(_title);
		titleLink.formats(ModelHelper.isMetamodel(model, box()) ? Set.of("h2", "appTitleStyle", "unselectable") : Set.of("h2", "appTitleStyle"));
		titleLink.onExecute(e -> showTitleEditor());
		refreshLanguagePill(release);
		if (session().user() == null) return;
		refreshOperations();
		userHome.path(PathHelper.dashboardPath(session()));
		settingsEditor.model(model);
		settingsEditor.mode(ModelSettingsEditor.Mode.Small);
		settingsEditor.refresh();
		cloneModelEditor.model(model);
		cloneModelEditor.mode(CloneModelEditor.Mode.Small);
		cloneModelEditor.refresh();
		cloneModelEditor.visible(ModelHelper.canClone(model, box()));
		build.visible(ModelHelper.canBuild(model, username(), box()));
		publishTrigger.visible(ModelHelper.canCreateRelease(model, username(), box()));
		dashboard.visible(user() != null);
	}

	private void refreshLanguagePill(Release release) {
		languagePath.visible(release.level() != LanguageLevel.L1);
		if (!languagePath.isVisible()) return;
		languagePath.path(PathHelper.languagePath(model.releasedLanguage()));
	}

	private void refreshModelsLinks() {
		ModelLevel level = ModelHelper.level(model, box());
		Model m3Model = ModelHelper.m3Model(model, box());
		Model m2Model = ModelHelper.m2Model(model, box());
		Release lastRelease = box().languageManager().lastRelease(model.releasedLanguage());
		LanguageLevel languageLevel = lastRelease != null ? lastRelease.level() : LanguageLevel.L1;
		ImeBrowser browser = new ImeBrowser(box());
		List<Browser.Model> m2Children = languageLevel == LanguageLevel.L2 ? browser.model(model.id()).children() : Collections.emptyList();
		List<Browser.Model> m1Children = languageLevel == LanguageLevel.L1 ? browser.model(model.id()).children() : Collections.emptyList();
		boolean m3Readonly = m3Model == null;
		boolean m2Readonly = level == ModelLevel.M3 || m2Model == null;
		boolean m1Readonly = level == ModelLevel.M2 || level == ModelLevel.M3;
		m3Link.visible(level != ModelLevel.M3);
		m3Link.readonly(m3Readonly);
		m3Link.path(PathHelper.modelPath(m3Model));
		m3.visible(level == ModelLevel.M3);
		this.m2Children.visible(!m2Children.isEmpty());
		this.m2Children.onOpen(e -> refreshBrowserModelChildrenView(m2Children));
		m2Link.visible(level != ModelLevel.M2 && m2Model != null && m2Children.isEmpty());
		m2Link.readonly(m2Readonly);
		m2Link.path(PathHelper.modelPath(m2Model));
		m2.visible(level == ModelLevel.M2 || (languageLevel == LanguageLevel.L2 && m2Children.isEmpty()));
		refreshStyles(m1Readonly, m2Readonly, m3Readonly);
		m1Link.visible(level != ModelLevel.M1 && m1Children.isEmpty());
		m1Link.readonly(m1Readonly);
		this.m1Children.visible(level != ModelLevel.M1 && languageLevel == LanguageLevel.L1 && !m1Children.isEmpty());
		this.m1Children.onOpen(e -> refreshBrowserModelChildrenView(m1Children));
		m1.visible(level == ModelLevel.M1 || (languageLevel == LanguageLevel.L1 && m1Children.isEmpty()));
	}

	private void refreshModelChildrenView(String title, List<Model> children) {
		modelChildrenCatalog.title(String.format(translate(title), ModelHelper.label(model, language(), box())));
		modelChildrenCatalog.readonly(true);
		modelChildrenCatalog.embedded(true);
		modelChildrenCatalog.source(new MemoryModelsDatasource(box(), session(), children));
		modelChildrenCatalog.refresh();
	}

	private void refreshBrowserModelChildrenView(List<Browser.Model> children) {
		ModelManager modelManager = box().modelManager();
		refreshModelChildrenView("Models using %s", children.stream().map(c -> modelManager.model(c.id())).toList());
	}

	private void refreshModelChildrenView(Release release) {
		List<Model> models = box().modelManager().allModels().stream().filter(m -> ModelHelper.canOpenModel(m, username()) && release.language().equals(Language.nameOf(m.modelingLanguage()))).toList();
		refreshModelChildrenView("Models similar to %s", models);
	}

	private void refreshStyles(boolean m1Readonly, boolean m2Readonly, boolean m3Readonly) {
		if (m3Readonly) m3Link.formats(Set.of("modelLinkDisabled", "m3Style"));
		if (m2Readonly) m2Link.formats(Set.of("modelLinkDisabled", "m2Style"));
		if (m1Readonly) m1Link.formats(Set.of("modelLinkDisabled", "m1Style"));
	}

	private void refreshOperations() {
		List<Operation> operationList = ModelHelper.operations(model, box());
		modelOperationsBlock.visible(!operationList.isEmpty());
		operations.clear();
		operationList.forEach(o -> fill(o, operations.add()));
	}

	private void fill(Operation operation, ModelOperation display) {
		display.operation(operation);
		display.onExecute(e -> execute(operation));
		display.refresh();
	}

	private void open(Model model) {
		openModelListener.accept(model);
		DelayerUtil.execute(this, v -> notifier.redirect(PathHelper.modelUrl(session(), model)), 600);
	}

	private void showTitleEditor() {
		if (ModelHelper.isMetamodel(model, box())) return;
		titleField.value(_title);
		titleLink.visible(false);
		titleEditor.visible(true);
		titleField.focus();
	}

	private void hideTitleEditor() {
		titleLink.visible(true);
		titleEditor.visible(false);
	}

	private void saveLabel() {
		_title = titleField.value();
		box().commands(ModelCommands.class).saveLabel(model, _title, username());
		hideTitleEditor();
		refresh();
	}

	private void refreshPublishDialog() {
		releaseEditor.model(model);
		releaseEditor.onAccept(e -> publish());
		releaseEditor.refresh();
	}

	private void build() {
		box().commands(ModelCommands.class).build(model, username());
		notifyUser(translate("Model built"), UserMessage.Type.Success);
	}

	private void publish() {
		if (!releaseEditor.check()) return;
		publishDialog.close();
		Release release = releaseEditor.release();
		box().commands(LanguageCommands.class).createRelease(model, release.level(), release.version(), username());
		notifyUser(String.format(translate("Release %s published"), release.version()), UserMessage.Type.Success);
	}

	private void execute(Operation operation) {
		// TODO MC
		box().commands(ModelCommands.class).execute(model, operation, username());
		notifyUser("Pendiente de implementar esta operación", UserMessage.Type.Warning);
	}

}