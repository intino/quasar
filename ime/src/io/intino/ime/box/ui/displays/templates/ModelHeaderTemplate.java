package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.utils.DelayerUtil;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.LanguageCommands;
import io.intino.ime.box.commands.ModelCommands;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.model.*;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class ModelHeaderTemplate extends AbstractModelHeaderTemplate<ImeBox> {
	private Model model;
	private String _title;
	private String descriptionValue = null;
	private String descriptionPath = null;
	private String descriptionFormat = null;
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

	public void description(String content, String path, String format) {
		this.descriptionValue = content;
		this.descriptionPath = path;
		this.descriptionFormat = format;
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
		removeModel.onExecute(e -> removeModel());
		login.onExecute(e -> {
			session().add("callback", session().browser().requestUrl());
			notifier.redirect(session().login(session().browser().baseUrl()));
		});
		publishDialog.onOpen(e -> refreshPublishDialog());
		publish.onExecute(e -> publish());
	}

	@Override
	public void refresh() {
		super.refresh();
		refreshModelsLinks();
		login.visible(user() == null);
		titleLink.title(_title);
		titleLink.onExecute(e -> showTitleEditor());
		description.title(descriptionValue);
		description.formats(Set.of("modelDescriptionStyle", descriptionFormat));
		description.path(descriptionPath);
		if (session().user() == null) return;
		refreshOperations();
		userHome.path(PathHelper.userHomePath(session()));
		settingsEditor.model(model);
		settingsEditor.mode(ModelSettingsEditor.Mode.Large);
		settingsEditor.refresh();
		removeModel.visible(ModelHelper.canRemove(model, box()));
		cloneModelEditor.model(model);
		cloneModelEditor.mode(CloneModelEditor.Mode.Large);
		cloneModelEditor.refresh();
		cloneModelEditor.visible(ModelHelper.canClone(model, box()));
		publishTrigger.visible(ModelHelper.canCreateRelease(model, username(), box()));
	}

	private void refreshModelsLinks() {
		ModelLevel level = ModelHelper.level(model, box());
		Model m3Model = ModelHelper.m3Model(model, box());
		Model m2Model = ModelHelper.m2Model(model, box());
		boolean m3Readonly = false;
		boolean m2Readonly = level == ModelLevel.M3;
		boolean m1Readonly = level == ModelLevel.M2 || level == ModelLevel.M3;
		m3Link.visible(level != ModelLevel.M3);
		m3Link.readonly(m3Readonly);
		m3Link.path(PathHelper.publicModelPath(m3Model));
		m3.visible(level == ModelLevel.M3);
		m2Link.visible(level != ModelLevel.M2);
		m2Link.readonly(m2Readonly);
		m2Link.path(PathHelper.publicModelPath(m2Model));
		m2.visible(level == ModelLevel.M2);
		refreshStyles(m1Readonly, m2Readonly, m3Readonly);
		m1Link.visible(level != ModelLevel.M1);
		m1Link.readonly(m1Readonly);
		m1.visible(level == ModelLevel.M1);
	}

	private void refreshStyles(boolean m1Readonly, boolean m2Readonly, boolean m3Readonly) {
		if (m3Readonly) m3Link.formats(Set.of("modelLinkDisabled"));
		if (m2Readonly) m2Link.formats(Set.of("modelLinkDisabled"));
		if (m1Readonly) m1Link.formats(Set.of("modelLinkDisabled"));
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

	private void removeModel() {
		box().commands(ModelCommands.class).remove(model, username());
		notifier.redirect(PathHelper.homeUrl(session()));
	}

	private void refreshPublishDialog() {
		releaseEditor.model(model);
		releaseEditor.onAccept(e -> publish());
		releaseEditor.refresh();
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