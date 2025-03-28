package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.Resource;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.displays.events.ChangeEvent;
import io.intino.alexandria.ui.displays.events.SelectionEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command.ExecutionResult;
import io.quassar.editor.box.commands.LanguageCommands;
import io.quassar.editor.box.commands.ModelCommands;
import io.quassar.editor.box.ui.types.VersionType;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
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
		logoField.onChange(this::updateLogo);
		versionTypeSelector.onSelect(this::updateVersion);
		versionTypeSelector.selection("revisionOption");
		levelSelector.selection("level1Option");
	}

	private void updateLogo(ChangeEvent event) {
		try {
			File tmpFile = logoFile();
			if (tmpFile.exists()) tmpFile.delete();
			Resource value = event.value();
			if (value != null) Files.write(tmpFile.toPath(), value.bytes());
			logoField.value(value != null ? tmpFile : null);
		} catch (IOException e) {
			Logger.error(e);
		}
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
		languagePropertiesBlock.visible(false); //!box().languageManager().exists(model.name())
		if (!languagePropertiesBlock.isVisible()) return;
		logoField.value((URL)null);
	}

	private void publish() {
		if (!check()) return;
		dialog.close();
		createLanguageIfNotExists();
		if (!createRelease().success()) return;
		hideUserNotification();
		publishListener.accept(model, version());
	}

	private void createLanguageIfNotExists() {
		if (!ModelHelper.isMetamodel(model, box())) return;
		if (box().languageManager().exists(model.name())) return;
		notifyUser(translate("Creating language..."), UserMessage.Type.Loading);
		box().commands(LanguageCommands.class).create(model.name(), model.language(), level(), model.description(), logoFile(), username());
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
		if (!languagePropertiesBlock.isVisible()) return true;
		if (!logoFile().exists()) {
			notifyUser(translate("Select logo"), UserMessage.Type.Warning);
			return false;
		}
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

	private File logoFile() {
		return new File(box().archetype().tmp().root(), model.name() + "-logo.png");
	}

}