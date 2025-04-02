package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.displays.events.SelectionEvent;
import io.intino.alexandria.ui.displays.events.actionable.ToggleEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command.ExecutionResult;
import io.quassar.editor.box.commands.LanguageCommands;
import io.quassar.editor.box.commands.ModelCommands;
import io.quassar.editor.box.ui.types.VersionType;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.io.File;
import java.util.function.BiConsumer;

public class PublishModelDialog extends AbstractPublishModelDialog<EditorBox> {
	private Model model;
	private BiConsumer<Model, String> publishListener;
	private BiConsumer<Model, ExecutionResult> publishFailureListener;

	public PublishModelDialog(EditorBox box) {
		super(box);
	}

	public void model(Model value) {
		this.model = value;
	}

	public void onPublish(BiConsumer<Model, String> listener) {
		this.publishListener = listener;
	}

	public void onPublishFailure(BiConsumer<Model, ExecutionResult> listener) {
		this.publishFailureListener = listener;
	}

	public void open() {
		dialog.open();
	}

	@Override
	public void init() {
		super.init();
		dialog.onOpen(e -> refreshDialog());
		create.onExecute(e -> publish());
		versionTypeSelector.onSelect(this::updateVersion);
		versionTypeSelector.selection("revisionOption");
		levelSelector.selection("level1Option");
		languageSwitch.onToggle(this::toggleLanguage);
	}

	private void refreshDialog() {
		String value = ModelHelper.nextVersion(model, VersionType.Revision, box());
		dialog.title("Publish %s".formatted(ModelHelper.label(model, language(), box())));
		refreshVersionBlock(value);
		refreshLanguageBlock();
	}

	private void refreshVersionBlock(String value) {
		versionPropertiesBlock.visible(true);
		if (!versionPropertiesBlock.isVisible()) return;
		version.value(value);
		versionTypeSelector.selection("revisionOption");
		versionTypeSelector.readonly(value.equals("1.0.0"));
		levelSelector.selection("level1Option");
	}

	private void refreshLanguageBlock() {
		boolean existsLanguage = box().languageManager().exists(model.name());
		Language parentLanguage = box().languageManager().get(model.language());
		languageSwitch.visible(parentLanguage != null && parentLanguage.isFoundational());
		languageSwitch.title(!existsLanguage ? "Create language with this model version" : "Update language with this model version");
		languageSwitch.state(ToggleEvent.State.Off);
	}

	private void publish() {
		if (!check()) return;
		dialog.close();
		if (!createRelease().success()) return;
		createOrUpdateLanguage();
		hideUserNotification();
		publishListener.accept(model, version());
	}

	private void createOrUpdateLanguage() {
		if (languageSwitch.state() == ToggleEvent.State.Off) return;
		if (box().languageManager().exists(model.name())) updateLanguage();
		else createLanguage();
	}

	private void createLanguage() {
		notifyUser(translate("Creating language..."), UserMessage.Type.Loading);
		box().commands(LanguageCommands.class).create(model.name(), version(), model.language(), level(), model.description(), username());
	}

	private void updateLanguage() {
		notifyUser(translate("Updating language..."), UserMessage.Type.Loading);
		box().commands(LanguageCommands.class).publish(model.name(), version(), level(), username());
	}

	private ExecutionResult createRelease() {
		notifyUser(translate("Creating release..."), UserMessage.Type.Loading);
		ExecutionResult result = box().commands(ModelCommands.class).createRelease(model, version(), username());
		if (!result.success()) publishFailureListener.accept(model, result);
		hideUserNotification();
		return result;
	}

	private String version() {
		String result = version.value();
		return result != null && !result.isEmpty() ? result : ModelHelper.nextVersion(model, VersionType.Revision, box());
	}

	private boolean check() {
		return true;
	}

	private void updateVersion(SelectionEvent event) {
		version.value(ModelHelper.nextVersion(model, versionType(event), box()));
	}

	private VersionType versionType(SelectionEvent event) {
		String selected = (String) event.selection().getFirst();
		if (selected.equals("revisionOption")) return VersionType.Revision;
		if (selected.equals("minorVersionOption")) return VersionType.MinorVersion;
		return VersionType.MajorVersion;
	}

	private Language.Level level() {
		String selected = levelSelector.selection().getFirst();
		if (selected.equals("level1Option")) return Language.Level.L1;
		return Language.Level.L2;
	}

	private void toggleLanguage(ToggleEvent event) {
		languagePropertiesBlock.visible(event.state() == ToggleEvent.State.On);
	}

}