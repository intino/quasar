package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.displays.events.SelectionEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command.CommandResult;
import io.quassar.editor.box.commands.ModelCommands;
import io.quassar.editor.box.ui.types.VersionType;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;
import io.quassar.editor.model.ModelRelease;

import java.util.List;
import java.util.function.BiConsumer;

public class CommitModelDialog extends AbstractCommitModelDialog<EditorBox> {
	private Model model;
	private BiConsumer<Model, String> commitListener;
	private BiConsumer<Model, CommandResult> commitFailureListener;
	private BiConsumer<Model, CommandResult> createReleaseListener;

	public CommitModelDialog(EditorBox box) {
		super(box);
	}

	public void model(Model value) {
		this.model = value;
	}

	public void onCommit(BiConsumer<Model, String> listener) {
		this.commitListener = listener;
	}

	public void onCommitFailure(BiConsumer<Model, CommandResult> listener) {
		this.commitFailureListener = listener;
	}

	public void onCreateRelease(BiConsumer<Model, CommandResult> listener) {
		this.createReleaseListener = listener;
	}

	public void open() {
		dialog.open();
	}

	@Override
	public void init() {
		super.init();
		dialog.onOpen(e -> refreshDialog());
		create.onExecute(e -> commit());
		versionTypeSelector.onSelect(this::updateVersion);
		versionTypeSelector.selection(translate("Revision"));
	}

	private void refreshDialog() {
		String value = ModelHelper.nextVersion(model, VersionType.Revision, box());
		dialog.title("Commit %s".formatted(ModelHelper.label(model, language(), box())));
		refreshVersionBlock(value);
	}

	private static final List<String> AllVersions = List.of("Major version", "Minor version", "Revision", "Replace existing");
	private static final List<String> Versions = List.of("Major version", "Minor version", "Revision");
	private void refreshVersionBlock(String value) {
		versionPropertiesBlock.visible(true);
		if (!versionPropertiesBlock.isVisible()) return;
		Language language = box().languageManager().getWithMetamodel(model);
		boolean versionInUse = box().modelManager().hasModelsWith(language, model.lastRelease());
		boolean hasVersions = model.lastRelease() != null;
		version.value(value);
		versionTypeSelector.clear();
		versionTypeSelector.addAll((versionInUse || !hasVersions ? Versions : AllVersions).stream().map(this::translate).toList());
		versionTypeSelector.selection(translate("Revision"));
		versionTypeSelector.readonly(value.equals("1.0.0"));
	}

	private void commit() {
		if (!check()) return;
		dialog.close();
		CommandResult result = createRelease();
		if (!result.success()) commitFailureListener.accept(model, result);
		else commitListener.accept(model, version());
	}

	private CommandResult createRelease() {
		notifyUser(translate("Committing..."), UserMessage.Type.Loading);
		CommandResult result = box().commands(ModelCommands.class).createRelease(model, version(), username());
		if (!result.success()) {
			hideUserNotification();
			commitFailureListener.accept(model, result);
		}
		else {
			createReleaseListener.accept(model, result);
			openFinishDialog();
		}
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
		if (selected.equals(translate("Revision"))) return VersionType.Revision;
		if (selected.equals(translate("Minor version"))) return VersionType.MinorVersion;
		if (selected.equals(translate("Replace existing"))) return VersionType.SnapshotVersion;
		return VersionType.MajorVersion;
	}

	private void openFinishDialog() {
		hideUserNotification();
		//notifyUser(translate("Your changes have been saved and are now part of the current release"), UserMessage.Type.Success);
	}

	private void openExecutionDialog(ModelRelease release) {
		executionLauncher.model(model);
		executionLauncher.release(release.version());
		executionLauncher.launch();
	}

	private void openDownloadModel(ModelRelease release) {
		downloadModelDialog.title(translate("%s %s generated").formatted(ModelHelper.label(model, language(), box()), release.version()));
		downloadModelDialog.model(model);
		downloadModelDialog.release(release.version());
		downloadModelDialog.open();
	}

}